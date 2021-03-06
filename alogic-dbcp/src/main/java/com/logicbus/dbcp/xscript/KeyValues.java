package com.logicbus.dbcp.xscript;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import com.alogic.xscript.ExecuteWatcher;
import com.alogic.xscript.Logiclet;
import com.alogic.xscript.LogicletContext;
import com.alogic.xscript.doc.XsObject;
import com.anysoft.util.Pair;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.dbcp.processor.Preprocessor;
import com.logicbus.dbcp.sql.DBTools;
import com.logicbus.dbcp.sql.ObjectMappingAdapter;
import com.logicbus.dbcp.sql.oma.KeyValue;

/**
 * 处理竖表形式的KeyValues
 * 
 * @author yyduan
 * @since 1.6.11.27
 * 
 * @version 1.6.11.36 [20180613 duanyy] <br>
 * - 支持对sql语句进行transform<br>
 */
public class KeyValues extends DBOperation{
	protected String sqlQuery = "";
	protected Preprocessor processor = null;
	protected String id;
	protected ObjectMappingAdapter<Pair<String,String>> adapter = new KeyValue();
	protected boolean transform = false;
	
	public KeyValues(String tag, Logiclet p) {
		super(tag, p);
	}
	
	@Override
	public void configure(Properties p){
		super.configure(p);
		sqlQuery = PropertiesConstants.getRaw(p, "sql", sqlQuery);
		id = PropertiesConstants.getString(p, "id", "$" + this.getXmlTag(),true);
		transform = PropertiesConstants.getBoolean(p,"transform",transform,true);
		processor = new Preprocessor(transform,sqlQuery);
	}

	@Override
	protected void onExecute(Connection conn, final XsObject root,final XsObject current, LogicletContext ctx,
			ExecuteWatcher watcher) {
		List<Object> data = new ArrayList<Object>();
		String sql = processor.process(ctx, data);
		
		if (debug){
			log("sql=" + sql,"debug");
			log("binded=" + data.toString(),"debug");
		}
		
		List<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
		DBTools.list(conn, result, adapter, sql, data.toArray());
		if (!result.isEmpty()){
			for (Pair<String,String> p:result){
				ctx.SetValue(p.key(), p.value());
			}
			ctx.SetValue(id, String.valueOf(result.size()));
		}else{
			ctx.SetValue(id, "0");
		}
	}
}
