package com.varsql.db.ext.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.varsql.core.db.MetaControlBean;
import com.varsql.core.db.meta.AbstractDBMeta;
import com.varsql.core.db.mybatis.SQLManager;
import com.varsql.core.db.mybatis.handler.resultset.IndexInfoHandler;
import com.varsql.core.db.mybatis.handler.resultset.TableInfoMysqlHandler;
import com.varsql.core.db.servicemenu.DBObjectType;
import com.varsql.core.db.servicemenu.ObjectTypeTabInfo;
import com.varsql.core.db.valueobject.DatabaseParamInfo;
import com.varsql.core.db.valueobject.IndexInfo;
import com.varsql.core.db.valueobject.ObjectInfo;
import com.varsql.core.db.valueobject.ServiceObject;
import com.varsql.core.db.valueobject.TableInfo;
import com.vartech.common.utils.VartechUtils;


/**
 *
 * @FileName  : MysqlDBMeta.java
 * @프로그램 설명 : mysql meta
 * @Date      : 2019. 7. 3.
 * @작성자      : ytkim
 * @변경이력 :
 */
public class MysqlDBMeta extends AbstractDBMeta{

	private final Logger logger = LoggerFactory.getLogger(MysqlDBMeta.class);

	public MysqlDBMeta(MetaControlBean dbInstanceFactory){
		super(dbInstanceFactory
			,new ServiceObject[] { 
				 new ServiceObject(DBObjectType.TABLE)
				, new ServiceObject(DBObjectType.VIEW)	
				, new ServiceObject(DBObjectType.FUNCTION)
				, new ServiceObject(DBObjectType.INDEX)
				, new ServiceObject(DBObjectType.PROCEDURE)
				, new ServiceObject(DBObjectType.TRIGGER,false,ObjectTypeTabInfo.MetadataTab.INFO ,ObjectTypeTabInfo.MetadataTab.DDL)
			}
		);
	}

	@Override
	public List getVersion(DatabaseParamInfo dataParamInfo)  {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("dbSystemView", dataParamInfo);
		}
	}
	
	@Override
	public List<String> getSchemas(DatabaseParamInfo dataParamInfo) throws SQLException {
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("schemaList", dataParamInfo);
		}
	}

	@Override
	public List<TableInfo> getTables(DatabaseParamInfo dataParamInfo) throws Exception {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("tableList", dataParamInfo);
		}
	}

	@Override
	public List<TableInfo> getTableMetadata(DatabaseParamInfo dataParamInfo,String... tableNmArr) throws Exception {
		logger.debug("getTableMetadata {}  tableArr :: {}",dataParamInfo, tableNmArr);
		return tableAndColumnsInfo(dataParamInfo,"tableMetadata" ,tableNmArr);
	}

	@Override
	public List<TableInfo> getViews(DatabaseParamInfo dataParamInfo) throws Exception {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("viewList", dataParamInfo);
		}
	}
	
	@Override
	public List<TableInfo> getViewMetadata(DatabaseParamInfo dataParamInfo,String... tableNmArr) throws Exception	{
		return tableAndColumnsInfo(dataParamInfo,"viewMetadata" ,tableNmArr);
	}

	@Override
	public List<ObjectInfo> getProcedures(DatabaseParamInfo dataParamInfo) throws Exception {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("procedureList", dataParamInfo);
		}
	}

	@Override
	public List<ObjectInfo> getProcedureMetadata(DatabaseParamInfo dataParamInfo, String... prodecureName) throws Exception {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("objectMetadataList", dataParamInfo);
		}
	}

	@Override
	public List<ObjectInfo> getFunctions(DatabaseParamInfo dataParamInfo) throws Exception {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("functionList", dataParamInfo);
		}
	}
	
	@Override
	public List<ObjectInfo> getFunctionMetadata(DatabaseParamInfo dataParamInfo, String... objNames) throws Exception {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("objectMetadataList", dataParamInfo);
		}
	}

	@Override
	public List getIndexs(DatabaseParamInfo dataParamInfo) throws Exception {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("indexList", dataParamInfo);
		}
	}
	@Override
	public List<IndexInfo> getIndexMetadata(DatabaseParamInfo dataParamInfo, String... indexName) throws Exception {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());

		IndexInfoHandler handler = new IndexInfoHandler(dbInstanceFactory.getDataTypeImpl());

		if(indexName!=null && indexName.length > 0){
			StringBuilder sb =new StringBuilder();

			List<String> indexNameList = new ArrayList<String>();

			boolean  addFlag = false;
			for (int i = 0; i < indexName.length; i++) {
				sb.append(addFlag ? ",":"" ).append("'").append(indexName[i]).append("'");

				addFlag = true;
				if(i!=0 && (i+1)%1000==0){
					indexNameList.add(sb.toString());
					sb =new StringBuilder();
					addFlag = false;
				}
			}

			if(sb.length() > 0){
				indexNameList.add(sb.toString());
			}

			dataParamInfo.addCustom(OBJECT_NAME_LIST_KEY, indexNameList);
		}
		
		try(SqlSession sqlSesseion = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());){
			sqlSesseion.select("indexMetadata" ,dataParamInfo , handler);
		}
		return handler.getIndexInfoList();
	}

	@Override
	public List getTriggers(DatabaseParamInfo dataParamInfo){
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("triggerList", dataParamInfo);
		}
	}

	@Override
	public List getTriggerMetadata(DatabaseParamInfo dataParamInfo, String... triggerArr) throws Exception {
		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		try (SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());) {
			return sqlSession.selectList("triggerMetadata", dataParamInfo);
		}
	}

	private List<TableInfo> tableAndColumnsInfo (DatabaseParamInfo dataParamInfo, String queryId, String... tableNmArr){

		dataParamInfo.setSchema(dataParamInfo.getSchema().toUpperCase());
		
		if(tableNmArr!=null  && tableNmArr.length > 0){
			StringBuilder sb =new StringBuilder();

			List<String> tableInfoList = new ArrayList<String>();

			boolean  addFlag = false;
			for (int i = 0; i < tableNmArr.length; i++) {
				sb.append(addFlag ? ",":"" ).append("'").append(tableNmArr[i]).append("'");

				addFlag = true;
				if(i!=0 && (i+1)%1000==0){
					tableInfoList.add(sb.toString());
					sb =new StringBuilder();
					addFlag = false;
				}
			}

			if(sb.length() > 0){
				tableInfoList.add(sb.toString());
			}

			dataParamInfo.addCustom(OBJECT_NAME_LIST_KEY, tableInfoList);
		}
		TableInfoMysqlHandler tableInfoMysqlHandler;
		
		logger.debug("tableAndColumnsInfo {} ",VartechUtils.reflectionToString(dataParamInfo));

		try(SqlSession sqlSession = SQLManager.getInstance().getSqlSession(dataParamInfo.getVconnid());){

			if("viewMetadata".equals(queryId)){
				tableInfoMysqlHandler = new TableInfoMysqlHandler(dbInstanceFactory.getDataTypeImpl(), sqlSession.selectList("viewList" ,dataParamInfo));
			}else{
				tableInfoMysqlHandler = new TableInfoMysqlHandler(dbInstanceFactory.getDataTypeImpl(), sqlSession.selectList("tableList" ,dataParamInfo));
	
				if(tableInfoMysqlHandler.getTableNameList() !=null  && tableInfoMysqlHandler.getTableNameList().size() > 0){
					dataParamInfo.addCustom(OBJECT_NAME_LIST_KEY, tableInfoMysqlHandler.getTableNameList());
				}
			}
	
			sqlSession.select(queryId ,dataParamInfo,tableInfoMysqlHandler);
		}

		return tableInfoMysqlHandler.getTableInfoList();
	}

	@Override
	public <T>T getExtensionMetadata(DatabaseParamInfo dataParamInfo, String serviceName, Map param) throws Exception {
		return null;
	}

}
