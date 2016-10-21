package io.landysh.inflor.java.core.dataStructures;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

import com.google.protobuf.InvalidProtocolBufferException;

import io.landysh.inflor.java.core.proto.FCSFrameProto.Message;
import io.landysh.inflor.java.core.proto.FCSFrameProto.Message.Keyword;
import io.landysh.inflor.java.core.utils.FCSUtils;
import io.landysh.inflor.java.core.proto.FCSFrameProto.Message.Dimension;


//don't use the default serializer, there is a protobuf spec.
@SuppressWarnings("serial")
public class ColumnStore extends DomainObject{

	private static final String DEFAULT_PREFFERED_NAME_KEYWORD = "$FIL";

	

	private TreeMap<String, FCSDimension> columnData;

	private HashMap<String, String> keywords;
	private String preferredName;

	// data properties
	private Integer rowCount = -1;
	// file details
	public String UUID;

	// minimal constructor, use with .load()
	public ColumnStore() {
		super(null);
	}

	/**
	 * Store keywords and numeric columns in a persistable object.
	 * 
	 * @param inKeywords
	 *            some annotation to get started with. Must be a valid FCS
	 *            header but may be added to later.
	 */
	public ColumnStore(HashMap<String, String> keywords, int rowCount) {
		this(null, keywords, rowCount);
	}
	
	public ColumnStore(String priorUUID, HashMap<String, String> keywords, int rowCount) {
		super(priorUUID);
		this.keywords = keywords;
		columnData = new TreeMap<String, FCSDimension>();
		this.rowCount = rowCount;
		preferredName = getKeywordValue(DEFAULT_PREFFERED_NAME_KEYWORD);
	}

	public void addColumn(String name, FCSDimension newDim) {
		if (rowCount == newDim.getSize()) {
			columnData.put(name, newDim);
		} else {
			throw new IllegalStateException("New dimension does not match frame size: " + rowCount.toString());
		}
	}

	public int getColumnCount() {
		return getColumnNames().length;
	}

	public String[] getColumnNames() {
		final int size = columnData.keySet().size();
		final String[] newArray = new String[size];
		final String[] columnNames = columnData.keySet().toArray(newArray);
		return columnNames;
	}

	public TreeMap<String, FCSDimension> getData() {
		return columnData;
	}

	public double[] getDimensionData(String displayName) {
		FCSDimension matchingDimension = FCSUtils.findCompatibleDimension(this, displayName);
		return matchingDimension.getData();
	}

	public HashMap<String, String> getKeywords() {
		return keywords;
	}

	public String getKeywordValue(String keyword) {
		String result = null;
		try {
			result = keywords.get(keyword).trim();
		} catch (NullPointerException npe) {
			// No operatoin, just return a null value.
		}
		return result;

	}


	public String getPrefferedName() {
		String name = UUID;
		if (this.preferredName != null) {
			name = this.preferredName;
		}
		return name;
	}

	public double[] getRow(int index) {
		final double[] row = new double[getColumnCount()];
		int i = 0;
		for (final String name : getColumnNames()) {
			row[i] = columnData.get(name).getData()[index];
			i++;
		}
		return row;
	}

	public int getRowCount() {
		return rowCount;
	}

	public FCSDimension getVector(String name) {
		return columnData.get(name);
	}

	public byte[] save() {
		// create the builder
		final Message.Builder messageBuilder = Message.newBuilder();

		// Should add UUID field here.
		messageBuilder.setEventCount(this.rowCount);
		messageBuilder.setId(ID);

		// add the dimension names.
		for (final String name : getColumnNames()) {
			messageBuilder.addDimNames(name);
		}

		// add the keywords.
		for (final String s : keywords.keySet()) {
			final String key = s;
			final String value = keywords.get(s);
			final Message.Keyword.Builder keyBuilder = Message.Keyword.newBuilder();
			keyBuilder.setKey(key);
			keyBuilder.setValue(value);
			final Message.Keyword newKeyword = keyBuilder.build();
			messageBuilder.addKeyword(newKeyword);
		}
		// add the FCS Dimensions.
		final Integer size = getColumnNames().length;
		for (int i = 0; i < size; i++) {
			final Message.Dimension.Builder dimBuilder = Message.Dimension.newBuilder();
			
			//Add required information.
			final String name = getColumnNames()[i];
			FCSDimension fcsdim = columnData.get(name);
			dimBuilder.setIndex(fcsdim.getIndex());
			dimBuilder.setPnn(fcsdim.getShortName());
			dimBuilder.setPns(fcsdim.getStainName());
			dimBuilder.setPneF1(fcsdim.getPNEF1());
			dimBuilder.setPneF2(fcsdim.getPNEF2());
			dimBuilder.setPnr(fcsdim.getRange());
			dimBuilder.setCompRef(fcsdim.getCompRef());
			dimBuilder.setId(fcsdim.ID);
			// Add the numeric data
			final double[] rawArray = columnData.get(name).getData();
			for (final double value : rawArray) {
				dimBuilder.addData(value);
			}
			final Message.Dimension dim = dimBuilder.build();
			messageBuilder.addDimension(dim);
		}

		// build the message
		final Message buffer = messageBuilder.build();
		final byte[] bytes = buffer.toByteArray();
		return bytes;
	}

	public void save(FileOutputStream out) throws IOException {
		final byte[] message = this.save();
		out.write(message);
		out.flush();
	}
	
	public static ColumnStore load(byte[] bytes) throws InvalidProtocolBufferException {
		final Message loadedMessage = Message.parseFrom(bytes);

		// Load the keywords
		final HashMap<String, String> keywords = new HashMap<String, String>();
		for (int i = 0; i < loadedMessage.getKeywordCount(); i++) {
			final Keyword keyword = loadedMessage.getKeyword(i);
			final String key = keyword.getKey();
			final String value = keyword.getValue();
			keywords.put(key, value);
		}
		// Load the vectors
		final int rowCount = loadedMessage.getEventCount();
		final ColumnStore columnStore = new ColumnStore(loadedMessage.getId(), keywords, rowCount);
		final int dimCount = loadedMessage.getDimensionCount();
		final String[] dimen = new String[dimCount];
		for (int j = 0; j < dimCount; j++) {
			Dimension dim = loadedMessage.getDimension(j);
			String priorUUID = dim.getId();
			dimen[j] = priorUUID;
			final FCSDimension values = new FCSDimension(priorUUID, columnStore.getRowCount(), dim.getIndex(), dim.getPnn(), 
					dim.getPns(), dim.getPneF1(), dim.getPneF2(), dim.getPnr(), dim.getCompRef());
			for (int i = 0; i < values.getSize(); i++) {
				values.getData()[i] = dim.getData(i);
			}
			columnStore.addColumn(priorUUID, values);
		}
		columnStore.setPreferredName(columnStore.getKeywordValue(DEFAULT_PREFFERED_NAME_KEYWORD));
		return columnStore;
	}

	public static ColumnStore load(FileInputStream input) throws Exception {
		final byte[] buffer = new byte[input.available()];
		input.read(buffer);
		final ColumnStore columnStore = ColumnStore.load(buffer);
		return columnStore;
	}

	public void setData(TreeMap<String, FCSDimension> allData) {
		columnData = allData;
		rowCount = allData.get(getColumnNames()[0]).getData().length;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	@Override
	public String toString() {
		return getPrefferedName();
	}
}