package inflor.core.transforms;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.management.RuntimeErrorException;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.logging.LogFactory;
import inflor.core.proto.TransformMapProto.TransformMap;
import inflor.core.proto.TransformMapProto.TransformMap.Builder;
import inflor.core.proto.TransformMapProto.TransformMap.Transform;
import inflor.core.proto.TransformMapProto.TransformMap.TransformEntry;
import inflor.core.utils.PlotUtils;

public class TransformSet {
	
	Map<String, AbstractTransform> map;
	
	public TransformSet(){
		map = new HashMap<>();
	}
	
	public void addTransformEntry(String key, AbstractTransform value){
		map.put(key, value);
	}
	
	public void remove(String key){
		map.remove(key);
	}
	
	public void optimize(FCSFrame df){
		df.getDimensionNames().parallelStream().forEach(m -> optimizeTransform(m, df));
	}

	private void optimizeTransform(String name, FCSFrame df) {
		AbstractTransform t = map.get(name);
		FCSDimension dim = df.getDimension(name);
		if (t.getType().equals(TransformType.LOGICLE)){
			((LogicleTransform) t).optimizeW(df.getDimension(name).getData());
		} else if (t instanceof LogrithmicTransform){
			((LogrithmicTransform) t).optimize(dim.getData());
		} else if (t instanceof BoundDisplayTransform){
			((BoundDisplayTransform) t).optimize(dim.getData());
		}
	}	
	
	public byte[] save(){
		TransformMap tBuilder = createMap();		
		return tBuilder.toByteArray();
	}

	private TransformMap createMap() {
		Builder tBuilder = TransformMap.newBuilder();
		for (Entry<String, AbstractTransform> e:map.entrySet()){
			AbstractTransform at = e.getValue();
			TransformEntry.Builder entryBuilder = TransformEntry.newBuilder();
			entryBuilder.setKey(e.getKey());
			
			Transform.Builder transformBuilder = Transform.newBuilder();
			transformBuilder.setId(at.getID());
			if (at.getType().equals(TransformType.LOGICLE)){
				LogicleTransform lt = (LogicleTransform) at;
				transformBuilder.setType(inflor.core.proto.TransformMapProto.TransformMap.TransformType.LOGICLE);
				transformBuilder.setLogicleT(lt.getT());
				transformBuilder.setLogicleW(lt.getW());
				transformBuilder.setLogicleM(lt.getM());
				transformBuilder.setLogicleA(lt.getA());
			} else if (at.getType().equals(TransformType.LOGARITHMIC)){
				LogrithmicTransform logT = (LogrithmicTransform) at;
				transformBuilder.setType(inflor.core.proto.TransformMapProto.TransformMap.TransformType.LOG);
				transformBuilder.setLogMin(logT.getMin());
				transformBuilder.setLogMax(logT.getMax());
			} else if (at.getType().equals(TransformType.BOUNDARY)){
				BoundDisplayTransform bdt = (BoundDisplayTransform) at;
				transformBuilder.setType(inflor.core.proto.TransformMapProto.TransformMap.TransformType.BOUNDARY);
				transformBuilder.setBoundMin(bdt.getMinRawValue());
				transformBuilder.setBoundMax(bdt.getMaxRawValue());
			}
			entryBuilder.setEntry(transformBuilder.build());
			tBuilder.addEntry(entryBuilder.build());
		}
		return tBuilder.build();
	}
	
	public static TransformSet load(byte[] bytes) throws InvalidProtocolBufferException{
		TransformSet s = new TransformSet();
		
		TransformMap tMap = TransformMap.parseFrom(bytes);
		
		for (int i=0;i<tMap.getEntryCount();i++){
			TransformEntry entry = tMap.getEntry(i);
			Transform serializedTransform = entry.getEntry();
			AbstractTransform loadedTransform = null;
			if (serializedTransform.getType().equals(inflor.core.proto.TransformMapProto.TransformMap.TransformType.LOGICLE)){
				loadedTransform = new LogicleTransform(
						serializedTransform.getLogicleT(), 
						serializedTransform.getLogicleW(), 
						serializedTransform.getLogicleM(), 
						serializedTransform.getLogicleA());
			} else if (serializedTransform.getType().equals(inflor.core.proto.TransformMapProto.TransformMap.TransformType.LOG)){
				loadedTransform = new LogrithmicTransform(serializedTransform.getLogMin(), serializedTransform.getLogMax());
			} else if (serializedTransform.getType().equals(inflor.core.proto.TransformMapProto.TransformMap.TransformType.BOUNDARY)){
				loadedTransform = new BoundDisplayTransform(serializedTransform.getBoundMin(), serializedTransform.getBoundMax());
			}
			if (loadedTransform!=null){
				s.addTransformEntry(entry.getKey(), loadedTransform);
			}
		}	
		return s;
	}

	public AbstractTransform get(String shortName) {
		if (map.containsKey(shortName)){
			return map.get(shortName);
		} else {
			return PlotUtils.createDefaultTransform(shortName);
		}
	}

	public Map<String, AbstractTransform> getMap() {
		return map;
	}

	public String saveToString() {
		final TransformMap buffer = createMap();		
		try {
			return JsonFormat.printer().print(buffer);
		} catch (InvalidProtocolBufferException e) {
			LogFactory.createLogger(this.getClass().getName()).log(Level.FINE, "Unable to serialize message to json.");
			return null;
		}
	}
	public static TransformSet loadFromProtoString(String previewString) throws InvalidProtocolBufferException {
		Builder mb = TransformMap.newBuilder();
		JsonFormat.parser().merge(previewString, mb);
		return TransformSet.load(mb.build().toByteArray());
	}

	public TransformSet deepCopy() {
		try {
			return TransformSet.load(this.save());
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException("Unable to copy object.", e);
		}
	}
}