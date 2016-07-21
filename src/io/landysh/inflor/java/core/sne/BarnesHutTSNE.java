package io.landysh.inflor.java.core.sne;

public class BarnesHutTSNE {
	
	private RandomSeed seed;
	private double[][] X;
	private int N;
	private int D;
	private double[][] Y;
	private int noDims;
	private double perplexity;
	private double theta;
	private int maxIter;
	private int stopLyingIter;
	private int momentumSwitchIter;
	
	public BarnesHutTSNE (double[][] X, int N, int D, double[][] Y, int no_dims, double perplexity, 
			double theta, boolean randomSeed, int max_iter, int stop_lying_iter, int mom_switch_iter){
		
		validateSettings();
		this.X = X;
		this.N = N;
		this.D = D;
		this.Y = Y;
		this.noDims = no_dims;
		this.perplexity = perplexity;
		this.theta = theta;
		// Set random seed
		if(randomSeed==true){
			seed = RandomSeed.CURRENT_TIME;
		} else {
			seed = RandomSeed.STATIC;
		}
		this.maxIter = max_iter;
		this.stopLyingIter = stop_lying_iter;
		this.momentumSwitchIter = mom_switch_iter;
	}
	
	
	
	private void validateSettings() {
		// Determine whether we are using an exact algorithm
		if(N-1<3*this.perplexity){
			throw new RuntimeException("Perplexity too large for the number of data points!\n");
		}
	}



	void run() {

		boolean exact = (theta==.0)?true:false;

		// Set learning parameters
		double total_time= 0.;
		clock_t start,end;
		double momentum=.5,final_momentum=.8;
		double eta=200.0;

		// Allocate some memory
		double[][] dY = new double[N][this.noDims]; 
		double[] uY= new double[N*noDims]; //(double*)malloc(N*no_dims*sizeof(double));
		double[] gains = new double[N*noDims];//(double*)malloc(N*no_dims*sizeof(double));

		for(int i=0;i<N*noDims;i++){
			uY[i]=.0;
		}
		
		for(int i=0;i<N*noDims;i++){
			gains[i]=1.0;
		}

		// Normalize input data (to prevent numerical problems)
		double max_X=.0;
		for(int i=0;i<N*D;i++){
			if(fabs(X[i])>max_X){
				max_X=fabs(X[i]);
				}for(int i=0;i<N*D;i++){X[i]/=max_X;
			}
		}

		// Compute input similarities for exact t-SNE
		double*P;
		int*row_P;
		int*col_P;
		double*val_P;
		if(exact==true){
			// Compute similarities
			printf("Exact?");
			P = new double[N][N];//(double*)malloc(N*N*sizeof(double));
			computeGaussianPerplexity(X,N,D,P,perplexity);

			// Symmetrize input similarities
			printf("Symmetrizing...\n");
			int nN=0;
			for(int n=0;n<N;n++){
				int mN=0;
				for(int m=n+1;m<N;m++){
					P[nN+m]+=P[mN+n];
					P[mN+n]=P[nN+m];
					mN+=N;}nN+=N;
			}
			double sum_P=.0;
			for(int i=0;i<N*N;i++){sum_P+=P[i];}
			for(int i=0;i<N*N;i++){P[i]/=sum_P;}
		}else{
			// Compute input similarities for approximate t-SNE
			// Compute asymmetric pairwise input similarities
			computeGaussianPerplexity(X,N,D,&row_P,&col_P,&val_P,perplexity,(int)(3*perplexity));

			// Symmetrize input similarities
			symmetrizeMatrix(&row_P,&col_P,&val_P,N);
			double sum_P=.0;
			for(int i=0;i<row_P[N];i++)sum_P+=val_P[i];
			for(int i=0;i<row_P[N];i++)val_P[i]/=sum_P;
		}
		// Lie about the P-values
		if(exact){
			for(int i=0;i<N*N;i++)P[i]*=12.0;
		} else {
			for(int i=0;i<row_P[N];i++){
				val_P[i]*=12.0;
			}
		}

		// Initialize solution (randomly)
		if(skip_random_init!=true){
			for(int i=0;i<N*noDims;i++){
				Y[i]=randn()*.0001;
			}
		}

		// Perform main training loop
		for(int iter=0;iter<max_iter;iter++){
			// Compute (approximate) gradient
			if(exact)computeExactGradient(P,Y,N,noDims,dY);
			else computeGradient(P,row_P,col_P,val_P,Y,N,noDims,dY,theta);

			// Update gains
			for(int i=0;i<N*noDims;i++){
				gains[i]=(sign(dY[i])!=sign(uY[i]))?(gains[i]+.2):(gains[i]*.8);
			}
			for(int i=0;i<N*noDims;i++){
				if(gains[i]<.01){
					gains[i]=.01;
				}
			}
				

		// Perform gradient update (with momentum and gains)
		for(int i=0;i<N*noDims;i++)
			uY[i]=momentum*uY[i]-eta*gains[i]*dY[i];
				for(int i=0;i<N*noDims;i++){
					Y[i]=Y[i]+uY[i];
				}

		// Make solution zero-mean
				zeroMean(Y,N,noDims);

		// Stop lying about the P-values after a while, and switch momentum
		if(iter==stop_lying_iter){
			if(exact){
				for(int i=0;i<N*N;i++)P[i]/=12.0;
			} else { 
				for(int i=0;i<row_P[N];i++){
					val_P[i]/=12.0;
				}
			}
		}
		if(iter==mom_switch_iter){
			momentum=final_momentum;
		}
		// Print out progress																																												// approximate
	}
	
	// Compute gradient of the t-SNE cost function (using Barnes-Hut algorithm)
	void computeGradient(double[][] P, int[][] inp_row_P, int[][] inp_col_P, double[][] inp_val_P, 
			double[][] Y, int N, int D, double[][] dC, double theta){

		// Construct space-partitioning tree on current map
		SPTree* tree = new SPTree(D, Y, N);

		// Compute all terms required for t-SNE gradient
		double sum_Q = .0;
		double[][] pos_f = new double[this.N][this.D]; 
		double[][] neg_f = new double[this.N][this.D]; 
		tree->computeEdgeForces(inp_row_P, inp_col_P, inp_val_P, N, pos_f);
		
		for(int n = 0; n < N; n++) {
			tree->computeNonEdgeForces(n, theta, neg_f + n * D, &sum_Q);
		}

		// Compute final t-SNE gradient
		for(int i = 0; i < N * D; i++) {
			dC[i] = pos_f[i] - (neg_f[i] / sum_Q);
		}
	}

	// Compute gradient of the t-SNE cost function (exact)
	void computeExactGradient(double[][] P, double[][] Y, int N, int D, double[][] dC) {

		// Make sure the current gradient contains zeros
		for(int i = 0; i < N * D; i++) {
			dC[i] = 0.0;
		}

		// Compute the squared Euclidean distance matrix
		double[][] DD = new double[N][N];
		
		computeSquaredEuclideanDistance(Y, N, D, DD);

		// Compute Q-matrix and normalization sum
		double[][] Q = new double[N][N];
		double sum_Q = .0;
		int nN = 0;
		for(int n = 0; n < N; n++) {
			for(int m = 0; m < N; m++) {
				if(n != m) {
					Q[nN + m] = 1 / (1 + DD[nN + m]);
					sum_Q += Q[nN + m];
				}
			}
			nN += N;
		}

		// Perform the computation of the gradient
		nN = 0;
		int nD = 0;
		for(int n = 0; n < N; n++) {
			int mD = 0;
			for(int m = 0; m < N; m++) {
				if(n != m) {
					double mult = (P[nN + m] - (Q[nN + m] / sum_Q)) * Q[nN + m];
					for(int d = 0; d < D; d++) {
						dC[nD + d] += (Y[nD + d] - Y[mD + d]) * mult;
					}
				}
				mD += D;
			}
			nN += N;
			nD += D;
		}
	}


	//Evaluate t-SNE cost function (exactly)
	double evaluateError(double[][] P, double[][] Y, int N, int D) {

	// Compute the squared Euclidean distance matrix
	double[][] DD = new double[N][N];//(double*) malloc(N * N * sizeof(double));
	double[][] Q = new double[N][N];//(double*) malloc(N * N * sizeof(double));
	computeSquaredEuclideanDistance(Y, N, D, DD);

	// Compute Q-matrix and normalization sum
	int nN = 0;
	//AH: Suspicous
	double sum_Q = Double.MIN_VALUE;
	for(int n = 0; n < N; n++) {
		for(int m = 0; m < N; m++) {
			if(n != m) {
				Q[nN + m] = 1 / (1 + DD[nN + m]);
				sum_Q += Q[nN + m];
			}
			else Q[nN + m] = Double.MIN_VALUE;
		}
		nN += N;
	}
	for(int i = 0; i < N * N; i++) Q[i] /= sum_Q;

	// Sum t-SNE error
	double C = .0;
	for(int n = 0; n < N * N; n++) {
		C += P[n] * log((P[n] + Double.MIN_VALUE) / (Q[n] + Double.MIN_VALUE));
	}
	return C;
}

	//Evaluate t-SNE cost function (approximately)
	double evaluateError(int[][] row_P, int[][] col_P, double[][] val_P, double[][] Y, int N, int D, double theta){

		// Get estimate of normalization term
		SPTree* tree = new SPTree(D, Y, N);
		double* buff = (double*) calloc(D, sizeof(double));
		double sum_Q = .0;
		for(int n = 0; n < N; n++) tree->computeNonEdgeForces(n, theta, buff, &sum_Q);

		// Loop over all edges to compute t-SNE error
		int ind1, ind2;
		double C = .0, Q;
		for(int n = 0; n < N; n++) {
			ind1 = n * D;
			for(int i = row_P[n]; i < row_P[n + 1]; i++) {
				Q = .0;
				ind2 = col_P[i] * D;
         for(int d = 0; d < D; d++) buff[d]  = Y[ind1 + d];
         for(int d = 0; d < D; d++) buff[d] -= Y[ind2 + d];
         for(int d = 0; d < D; d++) Q += buff[d] * buff[d];
         Q = (1.0 / (1.0 + Q)) / sum_Q;
         C += val_P[i] * log((val_P[i] + FLT_MIN) / (Q + FLT_MIN));
			}
		}
		return C;
	}

void computeGaussianPerplexity(double[][] X, int N, int D, double[][] P, double perplexity) {

	// Compute the squared Euclidean distance matrix
	double[][] DD = new double[N][N];//(double*) malloc(N * N * sizeof(double));
	computeSquaredEuclideanDistance(X, N, D, DD);

	// Compute the Gaussian kernel row by row
	int nN = 0;
	for(int n = 0; n < N; n++) {
		// Initialize some variables
		boolean found = false;
		double beta = 1.0;
		double min_beta = -Double.MAX_VALUE;
		double max_beta =  Double.MAX_VALUE;
		double tol = 1e-5;
		double sum_P;

		// Iterate until we found a good perplexity
		int iter = 0;
		while(!found && iter < 200) {

			// Compute Gaussian kernel row
			for(int m = 0; m < N; m++) P[nN + m] = exp(-beta * DD[nN + m]);
			P[nN + n] = Double.MIN_VALUE;

			// Compute entropy of current row
			sum_P = Double.MIN_VALUE;
			for(int m = 0; m < N; m++) sum_P += P[nN + m];
			double H = 0.0;
			for(int m = 0; m < N; m++) H += beta * (DD[nN + m] * P[nN + m]);
			H = (H / sum_P) + log(sum_P);

			// Evaluate whether the entropy is within the tolerance level
			double Hdiff = H - log(perplexity);
			if(Hdiff < tol && -Hdiff < tol) {
				found = true;
			}
			else {
				if(Hdiff > 0) {
					min_beta = beta;
					if(max_beta == DBL_MAX || max_beta == -DBL_MAX)
						beta *= 2.0;
					else
						beta = (beta + max_beta) / 2.0;
				}
				else {
					max_beta = beta;
					if(min_beta == -DBL_MAX || min_beta == DBL_MAX)
						beta /= 2.0;
					else
						beta = (beta + min_beta) / 2.0;
				}
			}

			// Update iteration counter
			iter++;
		}

		// Row normalize P
		for(int m = 0; m < N; m++) P[nN + m] /= sum_P;
     nN += N;
	}

	// Clean up memory
	free(DD); DD = NULL;
}

	// Compute input similarities with a fixed perplexity using ball trees (this
	// function allocates memory another function should free)
	void computeGaussianPerplexity(double[][] X, int N, int D, int[][] _row_P, int[][] _col_P, 
									double[][] _val_P, double perplexity, int K) {

		// Allocate the memory we need
		_row_P = new int[N+1]; // (unsigned int*)    malloc((N + 1) * sizeof(unsigned int));
		_col_P = new int[N*K]; // (unsigned int*)    calloc(N * K, sizeof(unsigned int));
		_val_P = new double[N*K]; //(double*) calloc(N * K, sizeof(double));
		int[] row_P = _row_P;
		int[] col_P = _col_P;
		double[] val_P = _val_P;
		double[] cur_P = new double[N-1];//(double*) malloc((N - 1) * sizeof(double));
		row_P[0] = 0;
		for(int n = 0; n < N; n++) {
			row_P[n + 1] = row_P[n] + K;
		}

		// Build ball tree on data set
		VpTree tree = new VpTree();

		vector<DataPoint> obj_X(N, DataPoint(D, -1, X));
		for(int n = 0; n < N; n++) {
			obj_X[n] = DataPoint(D, n, X + n * D);
		}
		tree->create(obj_X);

		// Loop over all points to find nearest neighbors
		printf("Building tree...\n");
		vector<DataPoint> indices;
		vector<double> distances;
		for(int n = 0; n < N; n++) {

			// Find nearest neighbors
			indices.clear();
			distances.clear();
			tree->search(obj_X[n], K + 1, &indices, &distances);

			// Initialize some variables for binary search
			bool found = false;
			double beta = 1.0;
			double min_beta = -DBL_MAX;
			double max_beta =  DBL_MAX;
			double tol = 1e-5;

			// Iterate until we found a good perplexity
			int iter = 0; double sum_P;
			while(!found && iter < 200) {

				// Compute Gaussian kernel row
				for(int m = 0; m < K; m++) cur_P[m] = exp(-beta * distances[m + 1] * distances[m + 1]);

				// Compute entropy of current row
				sum_P = DBL_MIN;
				for(int m = 0; m < K; m++) sum_P += cur_P[m];
				double H = .0;
				for(int m = 0; m < K; m++) H += beta * (distances[m + 1] * distances[m + 1] * cur_P[m]);
				H = (H / sum_P) + log(sum_P);

				// Evaluate whether the entropy is within the tolerance level
				double Hdiff = H - log(perplexity);
				if(Hdiff < tol && -Hdiff < tol) {
					found = true;
				} else {
					if(Hdiff > 0) {
						min_beta = beta;
						if(max_beta == DBL_MAX || max_beta == -DBL_MAX){
							beta *= 2.0;
						}else{
							beta = (beta + max_beta) / 2.0;
						}
					} else {
						max_beta = beta;
						if(min_beta == -DBL_MAX || min_beta == DBL_MAX){
							beta /= 2.0;
						} else{
							beta = (beta + min_beta) / 2.0;
						}
					}
				}

				// Update iteration counter
				iter++;
			}
		}

		// Row-normalize current row of P and store in matrix
		for(int m = 0; m < K; m++) cur_P[m] /= sum_P;
		for(int m = 0; m < K; m++) {
			col_P[row_P[n] + m] = (int) indices[m + 1].index();
			val_P[row_P[n] + m] = cur_P[m];
		}
	}

	// Symmetrizes a sparse matrix
	void symmetrizeMatrix(int[][] _row_P, int[][] _col_P, double[][] _val_P, int N) {

		// Get sparse matrix
		int[] row_P = _row_P;
		int[] col_P = _col_P;
		double[] val_P = _val_P;

		// Count number of elements and row counts of symmetric matrix
		int row_counts = new int[N]; //(int*) calloc(N, sizeof(int));
		for(int n = 0; n < N; n++) {
			for(int i = row_P[n]; i < row_P[n + 1]; i++) {

				// Check whether element (col_P[i], n) is present
				bool present = false;
				for(int m = row_P[col_P[i]]; m < row_P[col_P[i] + 1]; m++) {
					if(col_P[m] == n) present = true;
				}
				if(present) row_counts[n]++;
				else {
					row_counts[n]++;
					row_counts[col_P[i]]++;
				}
			}
		}
		int no_elem = 0;
		for(int n = 0; n < N; n++) no_elem += row_counts[n];

		// Allocate memory for symmetrized matrix
		int[] sym_row_P = new int[N];//(unsigned int*) malloc((N + 1) * sizeof(unsigned int));
		int[] sym_col_P = new int[no_elem];//(unsigned int*) malloc(no_elem * sizeof(unsigned int));
		double[] sym_val_P = new double[no_elem];//(double*) malloc(no_elem * sizeof(double));

		// Construct new row indices for symmetric matrix
		sym_row_P[0] = 0;
		for(int n = 0; n < N; n++) sym_row_P[n + 1] = sym_row_P[n] + row_counts[n];

		// Fill the result matrix
		int[] offset = new int[N];// (int*) calloc(N, sizeof(int));
		for(int n = 0; n < N; n++) {
			for(int i = row_P[n]; i < row_P[n + 1]; i++) {                                  // considering element(n, col_P[i])

				// Check whether element (col_P[i], n) is present
				boolean present = false;
				for(int m = row_P[col_P[i]]; m < row_P[col_P[i] + 1]; m++) {
					if(col_P[m] == n) {
						present = true;
						if(n <= col_P[i]) {                                                 // make sure we do not add elements twice
							sym_col_P[sym_row_P[n]        + offset[n]]        = col_P[i];
							sym_col_P[sym_row_P[col_P[i]] + offset[col_P[i]]] = n;
							sym_val_P[sym_row_P[n]        + offset[n]]        = val_P[i] + val_P[m];
							sym_val_P[sym_row_P[col_P[i]] + offset[col_P[i]]] = val_P[i] + val_P[m];
						}
					}
				}
				// If (col_P[i], n) is not present, there is no addition involved
				if(!present) {
					sym_col_P[sym_row_P[n]        + offset[n]]        = col_P[i];
					sym_col_P[sym_row_P[col_P[i]] + offset[col_P[i]]] = n;
					sym_val_P[sym_row_P[n]        + offset[n]]        = val_P[i];
					sym_val_P[sym_row_P[col_P[i]] + offset[col_P[i]]] = val_P[i];
				}

				// Update offsets
				if(!present || (present && n <= col_P[i])) {
					offset[n]++;
					if(col_P[i] != n) offset[col_P[i]]++;
				}
			}
		}
		// Divide the result by two
		for(int i = 0; i < no_elem; i++) sym_val_P[i] /= 2.0;

		// Return symmetrized matrices
		free(_row_P); _row_P = sym_row_P;
		free(_col_P); _col_P = sym_col_P;
		free(_val_P); _val_P = sym_val_P;
	}

	// Compute squared Euclidean distance matrix
	void computeSquaredEuclideanDistance(double[] X, int N, int D, double[] DD) {
		double[] XnD = X;
		for(int n = 0; n < N; ++n, XnD += D) {
			double[] XmD = XnD + D;
			double[] curr_elem = &DD[n*N + n];
			curr_elem = 0.0;
			double[] curr_elem_sym = curr_elem + N;
				for(int m = n + 1; m < N; ++m, XmD+=D, curr_elem_sym+=N) {
					(++curr_elem) = 0.0;
					for(int d = 0; d < D; ++d) {
						curr_elem += (XnD[d] - XmD[d]) * (XnD[d] - XmD[d]);
					}
					curr_elem_sym = *curr_elem;
				}
		}
	}

	// Makes data zero-mean
	void zeroMean(double[] X, int N, int D) {

		// Compute data mean
		double[] mean = new double[D]; //(double*) calloc(D, sizeof(double));
		int nD = 0;
		for(int n = 0; n < N; n++) {
			for(int d = 0; d < D; d++) {
				mean[d] += X[nD + d];
			}
		nD += D;
		}
		for(int d = 0; d < D; d++) {
			mean[d] /= (double) N;
		}

		// Subtract data mean
		nD = 0;
		for(int n = 0; n < N; n++) {
			for(int d = 0; d < D; d++) {
				X[nD + d] -= mean[d];
			}
		nD += D;
		}
	}


	//Generates a Gaussian random number
	double randn() {
		double x, y, radius;
		do {
			x = 2 * (rand() / ((double) RAND_MAX + 1)) - 1;
			y = 2 * (rand() / ((double) RAND_MAX + 1)) - 1;
			radius = (x * x) + (y * y);
		} while((radius >= 1.0) || (radius == 0.0));
		radius = sqrt(-2 * log(radius) / radius);
		x *= radius;
		y *= radius;
		return x;
	}
}