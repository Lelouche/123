package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.lubu.cyrptdbmodule.dbscheme.CDBColumn;
import ch.ethz.inf.vs.lubu.cyrptdbmodule.util.CDBUtil;

public class mOPEJob {
	
	public String query;
	
	public boolean doInsert;
	
	public List<mOPEWork> work;	
	
	public mOPEJob(String query, boolean doInsert) {
		this.query = query;
		this.doInsert = doInsert;
		work = new ArrayList<mOPEWork>();
	}
	
	public void addWork(mOPEWork toDO) {
		work.add(toDO);
	}
	
	public String getQuery() {
		return query;
	}

	public boolean isDoInsert() {
		return doInsert;
	}

	public List<mOPEWork> getWork() {
		return work;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject jo = new JSONObject();
		jo.put("Query", query);
		if(doInsert)
			jo.put("DoInsert", "1");
		else
			jo.put("DoInsert", "0");
		
		JSONArray arr = new JSONArray();
		for(mOPEWork w : work) {
			arr.put(w.toJSON());
		}
		jo.put("Work", arr);
		return jo;
	}




	public static class mOPEWork {

		private int ID;
		
		private String Tag;

		private String schemeName;
		
		private CDBColumn column;
		
		private String cipher;

		public mOPEWork(String tag, String schemeName, CDBColumn column, String cipher) {
			Tag = tag;
			this.column = column;
			this.cipher = cipher;
			this.schemeName = schemeName;
			this.ID = CDBUtil.getID();
		}

		public CDBColumn getColumn() {
			return this.column;
		}

		public String getTag() {
			return Tag;
		}

		public String getSchemeName() {
			return schemeName;
		}

		public String getTableName() {
			return column.getBelongsTo().getHashName();
		}

		public String getColumnOPE() {
			return column.getHashName(EncLayer.EncLayerType.OPE);
		}

		public String getColumnDET() {
			return column.getHashName(column.getMainType());
		}

		public String getCipher() {
			return cipher;
		}

		public int getID() {
			return ID;
		}
		
		public JSONObject toJSON() throws JSONException {
			JSONObject jo = new JSONObject();
			jo.put("ID", ID);
			jo.put("Tag", Tag);
			jo.put("SchemeName",schemeName);
			jo.put("TableName", getTableName());
			jo.put("ColumnOPE",  getColumnOPE());
			jo.put("ColumnDET", getColumnDET());
			jo.put("Cipher", cipher);
			return jo;
		}
			
	}
	
}
