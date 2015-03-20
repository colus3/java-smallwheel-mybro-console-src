package smallwheel.mybro.support.builder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import smallwheel.mybro.builder.dto.DtoClassBuilder;
import smallwheel.mybro.common.Constants;

/**
 * Mybatis�� SqlMapperBuilder Ŭ����
 * 
 * @author yeonhooo
 *
 */
public class SqlMapperBuilderForMybatis extends SqlMapperBuilder {
	
	/** 
	 * SqlMap.xml ������ �����. 
	 * @param table list 
	 * */
	@Override
	public void writeSqlMap(String tableName) {
		final Element root = new Element("mapper");
		final Element typeAlias = new Element("typeAlias");
		final Element resultMap = new Element("resultMap");
		final Element sql = new Element("sql");
		final Element insert = new Element("insert");
		final Element select = new Element("select");
		final Element selectOne = new Element("select");
		final Element update = new Element("update");
		final Element delete = new Element("delete");
		
		// root ��� ����
		root.setAttribute(makeAttribute("namespace", DtoClassBuilder.entityName));
		
		// typeAlias ��� ����
//		String typeAliasText = "class" + DtoClassBuilder.className;
		final String typeAliasText = DtoClassBuilder.className;
		typeAlias.setAttribute(makeAttribute("alias", typeAliasText));
		typeAlias.setAttribute(makeAttribute("type", DtoClassBuilder.className));		
		
		// resultMap ��� ����
		final String resultMapText = "ret" + DtoClassBuilder.className;
		resultMap.setAttribute(makeAttribute("type", typeAliasText));
		resultMap.setAttribute(makeAttribute("id", resultMapText));		
		
		// result ��� ����
		for (int i = 0; i < DtoClassBuilder.propertyNameList.length; i++) {
			Element result = new Element("result");
			result.setAttribute(makeAttribute("property", DtoClassBuilder.propertyNameList[i]));
//			result.setAttribute(makeAttribute("javaType", "string"));
			result.setAttribute(makeAttribute("column", DtoClassBuilder.dbColumnNameList[i]));
			result.setAttribute(makeAttribute("jdbcType", DtoClassBuilder.dbColumnTypeList[i]));
			resultMap.addContent(result);
		}
		
		// dynamicWhere sql map ����
		sql.setAttribute(makeAttribute("id", "dynamicWhere"));
		sql.addContent(makeDynamicWhere(tableName));
		
		// insert sql map ����
		insert.setAttribute(makeAttribute("id", "insert" + DtoClassBuilder.entityName));
		insert.setAttribute(makeAttribute("parameterType", typeAliasText));
		insert.addContent(makeInsertSqlMap(tableName));
		
		// select list sql map ����
		select.setAttribute(makeAttribute("id", "select" + DtoClassBuilder.entityName + "List"));
		select.setAttribute(makeAttribute("parameterType", typeAliasText));
		select.setAttribute(makeAttribute("resultType", typeAliasText));
		select.addContent(makeSelectSqlMap(tableName));
		// ���� WHERE�� ����
		select.addContent(addDynamicWhere(tableName));
		
		// select sql map ����
		selectOne.setAttribute(makeAttribute("id", "select" + DtoClassBuilder.entityName));
		selectOne.setAttribute(makeAttribute("parameterType", typeAliasText));
		selectOne.setAttribute(makeAttribute("resultType", typeAliasText));
		selectOne.addContent(makeSelectSqlMap(tableName));
		selectOne.addContent(makePrimaryKeyWhere(tableName));
		
		// update sql map ����
		update.setAttribute(makeAttribute("id", "update" + DtoClassBuilder.entityName));
		update.setAttribute(makeAttribute("parameterType", typeAliasText));
		update.addContent(makeUpdateSqlMapHead(tableName));
		update.addContent(makeDynamicUpdateSqlMap(tableName));
		update.addContent(makePrimaryKeyWhere(tableName));
		
		// delete sql map ����
		delete.setAttribute(makeAttribute("id", "delete" + DtoClassBuilder.entityName));
		delete.setAttribute(makeAttribute("parameterType", typeAliasText));
		delete.addContent(makeDeleteSqlMap(tableName));
		delete.addContent(makePrimaryKeyWhere(tableName));
		
		// root �� �߰�
		root.addContent(new Comment(" Use type aliases to avoid typing the full class name every time. "));
		root.addContent(typeAlias);
		root.addContent(resultMap);
		root.addContent("\n");
		
		root.addContent(new Comment(" Dynamic Where Condition "));
		root.addContent(sql);
		root.addContent("\n");
		
		root.addContent(new Comment(" Insert " + tableName + " "));
		root.addContent(insert);
		root.addContent("\n");
		
		root.addContent(new Comment(" Select " + tableName + " List "));
		root.addContent(select);
		root.addContent("\n");
		
		root.addContent(new Comment(" Select " + tableName + " "));
		root.addContent(selectOne);
		root.addContent("\n");
		
		root.addContent(new Comment(" Update " + tableName + " "));
		root.addContent(update);
		root.addContent("\n");
		
		root.addContent(new Comment(" Delete " + tableName + " "));
		root.addContent(delete);
		
		/* DTD ���� ��, ���Ϸ� ����
		 * iBatis
		 * 	<!DOCTYPE sqlMap      
		 * 		PUBLIC "-//ibatis.apache.org//DTD SQL Map 2.0//EN"	
		 * 		"http://ibatis.apache.org/dtd/sql-map-2.dtd">
		 * 
		 * MyBatis
		 * 	<!DOCTYPE mapper
		 * 		PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
		 * 		"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
		 */
		DocType docType = new DocType(Constants.Mapper.MYBATIS_ELEMENT_NAME, Constants.Mapper.MYBATIS_PUBLIC_ID, Constants.Mapper.MYBATIS_SYSTEM_ID);
		Document doc = new Document(root, docType);
		try {
			// ������ XML ���� �����Ѵ�.
			FileOutputStream fos = new FileOutputStream(Constants.Path.SQL_MAPPER_DES_DIR + DtoClassBuilder.entityName + "Mapper.xml");
			XMLOutputter serializer = new XMLOutputter();
//			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
			
			// �⺻ ���� ���¸� �ҷ��� �����Ѵ�.
			Format fm = serializer.getFormat();
			// ���ڵ� ����
			fm.setEncoding("UTF-8");
			// �θ�, �ڽ� �±׸� �����ϱ� ���� �� ������ ���Ѵ�.
			fm.setIndent("\t");
			// �±װ� �ٹٲ��� �����Ѵ�.
			fm.setLineSeparator("\n");
			
			// ������ XML ������ ������ set �Ѵ�.
			serializer.setFormat(fm);
			
			// doc �� ������ fos �Ͽ� ������ �����Ѵ�.
			serializer.output(doc, fos);
			
			fos.flush();
			fos.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���� WHERE�� ����
	 * @param tableName
	 * @return
	 */
	private Element makeDynamicWhere(String tableName) {
		Element dynamic = new Element("where");
		Element ifTest = null;
		
		/* if ��� ����
		 * <if test = 'propertyName != null and propertyName != "" >
		 */
		for (int i = 0; i < DtoClassBuilder.propertyNameList.length; i++) {
			
			if ("INT".equals(DtoClassBuilder.dbColumnTypeList[i].toUpperCase())) {
				ifTest = new Element("if");
				ifTest.setAttribute(makeAttribute("test", DtoClassBuilder.propertyNameList[i] + " > 0"));
				ifTest.addContent("\n\t\t\t\tAND " + DtoClassBuilder.dbColumnNameList[i] + " = #{" + DtoClassBuilder.propertyNameList[i] + "}\n\t\t\t");
				dynamic.addContent(ifTest);
			} else {
				ifTest = new Element("if");
				ifTest.setAttribute(makeAttribute("test", DtoClassBuilder.propertyNameList[i] + " != null and " + DtoClassBuilder.propertyNameList[i] + " != ''"));
				ifTest.addContent("\n\t\t\t\tAND " + DtoClassBuilder.dbColumnNameList[i] + " = #{" + DtoClassBuilder.propertyNameList[i] + "}\n\t\t\t");
				dynamic.addContent(ifTest);
			}
		}
		
		return dynamic;
	}
	
	/**
	 * PK �������� �̷��� WHERE�� ����
	 * @param tableName
	 * @return
	 */
	private String makePrimaryKeyWhere(String tableName) {
		String sql = "\n\t\t" + "WHERE";

		for (int i = 0; i < DtoClassBuilder.dbPrimaryKeyColumnNameList.size(); i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + DtoClassBuilder.dbPrimaryKeyColumnNameList.get(i) + " = #{" + DtoClassBuilder.propertyPrimaryKeyNameList.get(i) + "}";
			} else {
				sql = sql + "\n\t\t\t" + "AND " + DtoClassBuilder.dbPrimaryKeyColumnNameList.get(i) + " = #{" + DtoClassBuilder.propertyPrimaryKeyNameList.get(i) + "}";
			}
		}
		sql += "\n\t";
		return sql;
	}
	

	/**
	 * ������ WHERE�� �߰�
	 * @param tableName
	 * @return
	 */
	private Element addDynamicWhere(String tableName) {
		Element include = new Element("include");
		include.setAttribute(makeAttribute("refid", "dynamicWhere"));
		return include;
	}
	
	/** insert ������ �ۼ� */
	private String makeInsertSqlMap(String tableName) {
		String sql = "\n\t\tINSERT INTO " + tableName + " ( ";
		for (int i = 0; i < DtoClassBuilder.propertyNameList.length; i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + DtoClassBuilder.dbColumnNameList[i];
			} else {
				sql = sql + "\n\t\t\t" + "," + DtoClassBuilder.dbColumnNameList[i];
			}
		}
		sql += "\n\t\t) VALUES (";
		for (int i = 0; i < DtoClassBuilder.propertyNameList.length; i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t#{" + DtoClassBuilder.propertyNameList[i] + "} ";
			} else {
				sql = sql + "\n\t\t\t," + "#{" + DtoClassBuilder.propertyNameList[i] + "} ";
			}
		}
		sql += "\n\t\t);\n\t";
		return sql;
	}
	
	/** select ������ �ۼ� */
	private String makeSelectSqlMap(String tableName) {
		String sql = "\n\t\tSELECT ";
		for (int i = 0; i < DtoClassBuilder.propertyNameList.length; i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + DtoClassBuilder.dbColumnNameList[i] + "\tAS " + DtoClassBuilder.propertyNameList[i];
			} else {
				sql = sql + "\n\t\t\t" + "," + DtoClassBuilder.dbColumnNameList[i] + "\tAS " + DtoClassBuilder.propertyNameList[i];
			}
		}
		sql = sql + "\n\t\tFROM " + tableName + "\t\t";
		return sql;
	}
	
	/** update ������ �ۼ� */
	@SuppressWarnings("unused")
	private String makeUpdateSqlMap(String tableName) {
		String sql = "\n\t\tUPDATE " + tableName + " \n\t\tSET";
		for (int i = 0; i < DtoClassBuilder.propertyNameList.length; i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + DtoClassBuilder.dbColumnNameList[i] + " = " + "#{" + DtoClassBuilder.propertyNameList[i] + "} ";
			} else {
				sql = sql + "\n\t\t\t" + "," + DtoClassBuilder.dbColumnNameList[i] + " = " + "#{" + DtoClassBuilder.propertyNameList[i] + "} ";
			}
		}
		sql += "\n\t\t";
		return sql;
	}
	
	/**
	 * update ������ ���
	 * @param tableName
	 * @return
	 */
	private String makeUpdateSqlMapHead(String tableName) {
		String sql = "\n\t\tUPDATE " + tableName;
		return sql;
	}
	
	/**
	 * ���� update ������ �ۼ� 
	 * ��) <isNotEmpty property="applyName">,APPLY_NAME = #applyName# </isNotEmpty>
	 * 
	 * prepend �� ������� �ʴ� ������ ����
	 * @param tableName
	 * @return
	 */
	private Element makeDynamicUpdateSqlMap(String tableName) {
		
		Element dynamic = new Element("trim");
		dynamic.setAttribute(makeAttribute("prefix", "SET"));
		dynamic.setAttribute(makeAttribute("prefixOverrides", ","));
		
		Element ifTest = null;

		for (int i = 0; i < DtoClassBuilder.propertyNameList.length; i++) {
			
			if ("INT".equals(DtoClassBuilder.dbColumnTypeList[i].toUpperCase())) {
				ifTest = new Element("if");
				ifTest.setAttribute(makeAttribute("test", DtoClassBuilder.propertyNameList[i] + " > 0"));
				ifTest.addContent("\n\t\t\t\t, " + DtoClassBuilder.dbColumnNameList[i] + " = #{" + DtoClassBuilder.propertyNameList[i] + "}\n\t\t\t");
				dynamic.addContent(ifTest);
			} else {
				ifTest = new Element("if");
				ifTest.setAttribute(makeAttribute("test", DtoClassBuilder.propertyNameList[i] + " != null and " + DtoClassBuilder.propertyNameList[i] + " != ''"));
				ifTest.addContent("\n\t\t\t\t, " + DtoClassBuilder.dbColumnNameList[i] + " = #{" + DtoClassBuilder.propertyNameList[i] + "}\n\t\t\t");
				dynamic.addContent(ifTest);
			}
			
		}
		
		return dynamic;
	}
	
	/** delete ������ �ۼ� */
	private String makeDeleteSqlMap(String tableName) {
		String sql = "\n\t\tDELETE FROM " + tableName + "\t\t";
		return sql;
	}
	
	/**
	 * Attribute �� �����Ͽ� ��ȯ�Ѵ�.
	 * 
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	private Attribute makeAttribute(String attributeName, String attributeValue) {
		Attribute attribute = new Attribute(attributeName, attributeValue); 
		return attribute;
	}
	
	
}
