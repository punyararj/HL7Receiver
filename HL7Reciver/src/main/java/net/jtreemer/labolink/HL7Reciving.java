package net.jtreemer.labolink;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.datatype.NM;
import ca.uhn.hl7v2.model.v23.datatype.ST;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v23.message.ACK;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.OBR;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.model.v23.segment.PV1;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import net.jtreemer.labolink.db.DBUtils;
import net.jtreemer.labolink.model.LinkData;
import net.jtreemer.labolink.model.ResultItem;

public class HL7Reciving implements ReceivingApplication<Message> {

	private String filePath;
	private String fileName;

	public HL7Reciving(String fpath,String fname) {
		filePath = fpath;
		fileName = fname;
	}

	public boolean canProcess(Message arg0) {
		return true;
	}

	public Message processMessage(Message arg0, Map<String, Object> arg1)
			throws ReceivingApplicationException, HL7Exception{


		Message msg=null;
		try {
			if(arg0 instanceof ORU_R01){
				ORU_R01 oru = (ORU_R01)arg0;
				MSH msh = oru.getMSH();
				
				String cid = oru.getRESPONSE().getPATIENT().getPID().getAlternatePatientID().getID().getValue();
				PV1 pv1 = oru.getRESPONSE().getPATIENT().getVISIT().getPV1();
				String vn = pv1.getVisitNumber().getID().getValue();
				String patientId = oru.getRESPONSE().getPATIENT().getPID().getPatientIDInternalID(0).getID().getValue();
				String deviceId = msh.getSendingApplication().getNamespaceID().getValue();
				String msgDateTime = msh.getDateTimeOfMessage().getTimeOfAnEvent().getValue();
				String mode = Main.AppConfig.getProperty("export.mode", "file");
				if("LABOLINK_TEST".equals(deviceId)) {
					System.out.println("Reject data from "+deviceId);
					return arg0.generateACK(AcknowledgmentCode.AR, new HL7Exception("Rejected Test Message"));
				}
				msg = arg0.generateACK();

				String encStr = msg.encode();
				if(mode.equals("file")){
					String fPath = filePath;
					String fName = fileName;
					fPath = fPath.replace("%devId%", deviceId);
					fPath = fPath.replace("%pid%", patientId);
					fPath = fPath.replace("%msgDt%", msgDateTime);

					fName = fName.replace("%devId%", deviceId);
					fName = fName.replace("%pid%", patientId);
					fName = fName.replace("%msgDt%", msgDateTime);
					
					File dir = new File(fPath);
					if(!dir.isDirectory()){
						dir.mkdirs();
					}
					if(fPath==""){
						fPath=".";
					}
					File outFile = new File(fPath+File.separator+fName);
					if(outFile.exists() && outFile.isFile()){
						System.out.println("Duplicate file delete");
						outFile.delete();
					}else {
						System.out.println(msgDateTime+"Recived data from "+deviceId+" "+patientId);
					}
					
					FileWriter fileWriter = new FileWriter(fPath+File.separator+fName);
					fileWriter.write(oru.encode());
					fileWriter.close();
				}else{
					LinkData linkData = new LinkData();
					linkData.setDeviceId(deviceId);
					linkData.setPatientHN(patientId);
					linkData.setPatientCID(patientId);
					linkData.setPatientVN(vn);
					linkData.setPatientCID(cid);
					ORU_R01_ORDER_OBSERVATION observation = oru.getRESPONSE().getORDER_OBSERVATION();
					OBR obr = observation.getOBR();
					linkData.setDataDateTime(obr.getRequestedDateTime().getTimeOfAnEvent().getValueAsDate());
					
					for(ORU_R01_OBSERVATION ob: observation.getOBSERVATIONAll()){
						ResultItem item = new ResultItem();
						OBX obx = ob.getOBX();
						String name= obx.getObservationIdentifier().getIdentifier().getValue();
						item.setName(name);
						String value = ((ST)obx.getObservationValue(0).getData()).getValue();
						item.setValue(value);
						String unit = obx.getUnits().getIdentifier().getValue();
						item.setUnit(unit);
						linkData.getResults().add(item);
					}
					try {
						List<Map<String, Object>> dts = DBUtils.toPack(linkData);
						DBUtils db = new DBUtils();
						for(Map<String, Object> di : dts){
							try {
								db.insertFlat(di);
							}catch (Exception e) {
								// TODO: handle exception
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
						//msg = msg.generateACK(AcknowledgmentCode.AE, new HL7Exception("Error Processing ID"));
						
					}
				}
				
				System.out.println(encStr);

				//ACK ackmsg = (ACK)msg;
				//ackmsg.getMSH().getReceivingApplication().getNamespaceID().setValue("TEST");
				//msg = ackmsg;
			}else{
				msg = arg0.generateACK(AcknowledgmentCode.AE, new HL7Exception("unhandle message"));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return msg;
	}



}
