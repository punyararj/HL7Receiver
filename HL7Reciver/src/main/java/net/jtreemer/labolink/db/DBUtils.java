package net.jtreemer.labolink.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.type.DoubleType;

import net.jtreemer.labolink.Main;
import net.jtreemer.labolink.model.LinkData;
import net.jtreemer.labolink.model.ResultItem;

public class DBUtils {



	public void insertFlat(Map<String,Object> data){
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		if(sessionFactory!=null){
			String insertSQL = Main.AppConfig.getProperty("sql.insert");
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			try{
				SQLQuery query = session.createSQLQuery(insertSQL);
				String[] params = query.getNamedParameters();
				for(String p : params){
					if(data.containsKey(p)){
						if(p.startsWith("VAL_")) {
							Double d = (Double)data.get(p);
							query.setParameter(p, d);
						}else {
							query.setParameter(p, data.get(p));
						}
					}else{
						if(p.startsWith("VAL_")) {
							query.setParameter(p, null, DoubleType.INSTANCE);
						}else {
							query.setParameter(p, null);
						}
					}
				}
				query.executeUpdate();
				session.flush();
				tx.commit();
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				try{
					session.close();
				}catch (Exception e) {
				}
			}

		}
	}

	public static List<Map<String,Object>> toPack(LinkData data){
		List<Map<String,Object>> result = new ArrayList<>();
		String hn = data.getPatientHN();
		String hnsplit[] = hn.split("/");
		System.out.println("Recived HN "+hn);
		HashMap<String,Object> map = new HashMap<>();
		map.put("DEV_ID", data.getDeviceId());
		map.put("CID", data.getPatientCID());
		map.put("HN", Integer.parseInt(hnsplit[0],10));
		map.put("HN_YEAR", Integer.parseInt(hnsplit[1],10));
		map.put("VN", data.getPatientVN());
		map.put("MSGTIME", data.getDataDateTime());
		for(ResultItem item : data.getResults()){
			map.put("VAL_"+item.getName().toUpperCase(), Double.parseDouble(item.getValue()));
			map.put("UNIT_"+item.getName().toUpperCase(), item.getUnit());
		}
		result.add(map);

		return result;
	}




}
