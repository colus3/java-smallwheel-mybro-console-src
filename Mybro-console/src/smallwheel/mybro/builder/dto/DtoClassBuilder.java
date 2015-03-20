package smallwheel.mybro.builder.dto;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import smallwheel.mybro.common.Constants;
import smallwheel.mybro.common.ENV;

/**
 * 
 * @author yeonhooo
 *
 */
public class DtoClassBuilder {
	
	private final static Logger LOGGER = Logger.getLogger(DtoClassBuilder.class);
	static String classNameSuffix = "Dto";
	public static String entityName;
	public static String className;
	public static String[] dbColumnNameList;	// from
	public static String[] dbColumnTypeList;	// from
	public static List<String> dbPrimaryKeyColumnNameList;	// from
	public static String[] propertyNameList;	// to
	public static String[] propertyTypeList;	// to
	public static List<String> propertyPrimaryKeyNameList;	// to
	
	/**
	 * @param index 
	 * @param con 
	 * @param TABLE_LIST
	 */
	public void makeModelClassFileByTable(int index, Connection con, String tableName, String prefixExcept) {
		PreparedStatement pstmt;
		DatabaseMetaData databaseMetaData;
		ResultSet rs;
		ResultSetMetaData rm;
		
		try
		{
			pstmt = con.prepareStatement("select * from " + tableName + " where 1=0");
			databaseMetaData = con.getMetaData();
			rs = pstmt.executeQuery();			
			rm = rs.getMetaData();
			
			// DB Column ������ �����´�.
			dbColumnNameList = new String[rm.getColumnCount()];
			dbColumnTypeList = new String[rm.getColumnCount()];
			for (int i = 1; i <= rm.getColumnCount(); i++) {
				dbColumnNameList[i - 1] = rm.getColumnName(i);
				dbColumnTypeList[i - 1] = rm.getColumnTypeName(i);
			}
			
			// PK Ȯ��
			dbPrimaryKeyColumnNameList = new ArrayList<String>();
			propertyPrimaryKeyNameList = new ArrayList<String>();
			ResultSet keys = databaseMetaData.getPrimaryKeys(null, null, tableName);
			while (keys.next()) {
				dbPrimaryKeyColumnNameList.add(keys.getString("COLUMN_NAME"));
				propertyPrimaryKeyNameList.add(makePropertyName(keys.getString("COLUMN_NAME")));
			}
			
			// Check
			LOGGER.info("[Table Name: " + tableName + " / Column Count: " + rm.getColumnCount() + "]");
			LOGGER.info("PK Columns" );
			for(String key : dbPrimaryKeyColumnNameList){
				LOGGER.info("\t" + key);
			}
			
			// EntityName �� �����
			entityName = makeEntityName(tableName, prefixExcept);
			
			// ClassName �� �����.
			className = makeClassName(entityName);
			FileWriter writer = new FileWriter(Constants.Path.DTO_CLASS_DES_DIR + className + ".java");
			
			// Class �ۼ� ����
			writer.write("public class " + className + " {");
			
			// Property �ۼ�
			writer.write("\n\t" + "/* properties */" + "\n");
			propertyNameList = new String[rm.getColumnCount()];
			propertyTypeList = new String[rm.getColumnCount()];
			for (int i = 0; i < rm.getColumnCount(); i++) {
				propertyNameList[i] = makePropertyName(dbColumnNameList[i]);
				propertyTypeList[i] = makePropertyType(dbColumnTypeList[i]);
				writer.write("\t" + "private " + propertyTypeList[i] + " " + propertyNameList[i] + ";" + "\n");
			}
			
			// Getter, Setter �ۼ�
			writer.write("\n\t" + "/* getter, setter */" + "\n");
			for (int i = 0; i < rm.getColumnCount(); i++) {
				String columnName = propertyNameList[i];				
				columnName = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
				
				// Getter
				writer.write("\t" + "public " + propertyTypeList[i] + " get" + columnName + "() {" + "\n");
				writer.write("\t\t" + "return " + propertyNameList[i] + ";" + "\n\t" + "}" + "\n");
				
				// Setter
				writer.write("\t" + "public void set" + columnName + "(" + propertyTypeList[i] + " " + propertyNameList[i] + ") {" + "\n");
				writer.write("\t\t" + "this." + propertyNameList[i] + " = " + propertyNameList[i] + ";" + "\n\t" + "}" + "\n");
			}
			
			// Class �ݱ�
			writer.write("\n}");
			
			rs.close();
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param index 
	 * @param con
	 * @param ���ν��� ����Ʈ
	 */
	public void makeModelClassFileByProcedure(int index, Connection con, String procedureName) {
		PreparedStatement pstmt;
		ResultSet rs;
		ResultSetMetaData rm;
		
		try
		{
			pstmt = con.prepareStatement(procedureName);
			rs = pstmt.executeQuery();			
			rm = rs.getMetaData();
			
			// DB Column �� �����´�.
			dbColumnNameList = new String[rm.getColumnCount()];
			for (int i = 1; i <= rm.getColumnCount(); i++) {
				dbColumnNameList[i - 1] = rm.getColumnName(i);
			}
			
			// Check
			LOGGER.info("[Procedure Name: " + procedureName + " / Column Count: " + rm.getColumnCount() + "]\n");						
			
			// ClassName �� �����.
			className = makeClassName(procedureName);
			FileWriter writer = new FileWriter(Constants.Path.DTO_CLASS_DES_DIR + className+ ".java");
			
			// Class �ۼ� ����
			writer.write("public class " + className + " {");
			
			// Property �ۼ�
			writer.write("\n\t" + "/** properties */" + "\n");
			propertyNameList = new String[rm.getColumnCount()];
			propertyTypeList = new String[rm.getColumnCount()];
			for (int i = 0; i < rm.getColumnCount(); i++) {
				propertyNameList[i] = makePropertyName(dbColumnNameList[i]);
				propertyTypeList[i] = makePropertyType(dbColumnTypeList[i]);
				writer.write("\t" + "private " + propertyTypeList[i] + " " + propertyNameList[i] + " = \"\";" + "\n");
			}
			
			// Getter, Setter �ۼ�
			writer.write("\n\t" + "/** getter, setter */" + "\n");
			for (int i = 0; i < rm.getColumnCount(); i++) {
				String columnName = propertyNameList[i];				
				columnName = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
				
				// Getter
				writer.write("\t" + "public " + propertyTypeList[i] + " get" + columnName + "() {" + "\n");
				writer.write("\t\t" + "return " + propertyNameList[i] + ";" + "\n\t" + "}" + "\n");
				
				// Setter
				writer.write("\t" + "public void set" + columnName + "(" + propertyTypeList[i] + " " + propertyNameList[i] + ") {" + "\n");
				writer.write("\t\t" + "this." + propertyNameList[i] + " = " + propertyNameList[i] + ";" + "\n\t" + "}" + "\n");
			}
			
			// Class �ݱ�
			writer.write("\n}");
			
			rs.close();
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ƼƼ���� �����.
	 * 
	 * @param prefixExcept ��ƼƼ���� ������ ���ڿ� 
	 */
	private String makeEntityName(String tableName, String prefixExcept) {
		
		// prefixExcept�� ��ƼƼ���� �����Ѵ�.
		tableName = tableName.replaceAll(prefixExcept, "");
		tableName = tableName.toLowerCase(Locale.ENGLISH);
		
		while (true) {
			if (tableName.indexOf("_") > -1) {
				tableName = (tableName.substring(0, tableName.indexOf("_"))
				+ tableName.substring(tableName.indexOf("_") + 1, tableName.indexOf("_") + 2).toUpperCase()
				+ tableName.substring(tableName.indexOf("_") + 2)).trim();
			} else {
				break;
			}
		}
		
		// ù ���ڸ� �빮�ڷ� �����Ѵ�.
		tableName = tableName.substring(0, 1).toUpperCase() + tableName.substring(1);		
		return tableName;
	}
	
	/** Ŭ�������� �����. */
	private String makeClassName(String entityName) {
		return entityName + classNameSuffix;
	}
	
	/** Ŭ�������� ������Ƽ��(property)�� �����. 
	 * @param ��ȯ �� ���� DB �÷���
	 * @return ��ȯ �� ������Ƽ��(DB �÷���� ��Ī)
	 * */
	private String makePropertyName(String columnName) {
		columnName = columnName.toLowerCase(Locale.ENGLISH);
		
		while (true) {
			if (columnName.indexOf("_") > -1) {
				columnName = (columnName.substring(0, columnName.indexOf("_"))
				+ columnName.substring(columnName.indexOf("_") + 1, columnName.indexOf("_") + 2).toUpperCase()
				+ columnName.substring(columnName.indexOf("_") + 2)).trim();
			} else {
				break;
			}
		}
		
//		// ����� ������Ƽ���� �ڹ� ������̰ų�, ���������� ��쿡 ���� ó��		
//		if (columnName.equals("continue")) {
//			columnName = "continues";
//		} else if (columnName.equals("r")) {
//			columnName = "run";
//		} else if (columnName.equals("w")) {
//			columnName = "win";
//		} else if (columnName.equals("l")) {
//			columnName = "lose";
//		} else if (columnName.equals("d")) {
//			columnName = "draw";
//		} else if (columnName.equals("s")) {
//			columnName = "save";
//		}
//				
		return columnName;
	}
	
	
	/** ȯ�� ���� ���Ͽ� ������ ���յ��� ���� Ŭ������ ������Ƽ Ÿ���� �����.<br /><br />
	 * <strong>���յ� Ÿ��</strong><br /> 
	 * 
	 * ����(<code>HIGH</code>): DB�� ������ Ÿ���� �ڹ� ������Ƽ Ÿ�� ��ȯ<br />
	 * ����(<code>MIDDLE</code>): DB Ÿ�� �� �������� ��¥���� ����. �� �� Ÿ���� ���ڿ� Ÿ������ ��ȯ<br />
	 * ����(<code>LOW</code>): DB Ÿ�� �� �������� ��ȯ. �� �� Ÿ���� ���ڿ� Ÿ������ ��ȯ<br />
	 * ����(<code>NO</code>): ��� DB Ÿ���� ���ڿ� Ÿ������ ��ȯ
	 * 
	 * @param ��ȯ �� ���� DB �÷� Ÿ��
	 * @return ��ȯ �� ������Ƽ Ÿ��(DB �÷� Ÿ�԰� ��Ī)
	 * */
	private String makePropertyType(String columnType) {
		String propertyType = columnType.toUpperCase();
		
		if ("HIGH".equals(ENV.couplingType)) {
			// ���յ� ����
			if (propertyType.equals("TINYINT")
					|| propertyType.equals("SMALLINT")
					|| propertyType.equals("MEDIUMINT")
					|| propertyType.equals("INT")
					|| propertyType.equals("BIGINT")) {
				propertyType = "int";
			} else if (propertyType.equals("FLOAT")) {
				propertyType = "float";
			} else if (propertyType.equals("DOUBLE")
					|| propertyType.equals("DECIMAL")) {
				propertyType = "double";
			} else if (propertyType.equals("CHAR")
					|| propertyType.equals("VARCHAR")
					|| propertyType.equals("TEXT")
					|| propertyType.indexOf("BLOB") > -1) {
				propertyType = "String";
			} else if (propertyType.equals("DATE")
					|| propertyType.equals("DATETIME")
					|| propertyType.equals("TIMESTAMP")	) {
				propertyType = "Date";
			} else {
				propertyType = "String";
			}

		} else if ("MIDDLE".equals(ENV.couplingType)) {
			// ���յ� ����
			if (propertyType.equals("TINYINT")
					|| propertyType.equals("SMALLINT")
					|| propertyType.equals("MEDIUMINT")
					|| propertyType.equals("INT")
					|| propertyType.equals("BIGINT")) {
				propertyType = "int";
			} else if (propertyType.equals("DATE")
					|| propertyType.equals("DATETIME")
					|| propertyType.equals("TIMESTAMP")	) {
				propertyType = "Date";
			} else {
				propertyType = "String";
			}

		} else if ("LOW".equals(ENV.couplingType)) {
			// ���յ� ����
			if (propertyType.equals("TINYINT")
					|| propertyType.equals("SMALLINT")
					|| propertyType.equals("MEDIUMINT")
					|| propertyType.equals("INT")
					|| propertyType.equals("BIGINT")) {
				propertyType = "int";
			} else {
				propertyType = "String";
			}
			
		} else if ("NO".equals(ENV.couplingType)) {
			// ���յ� ����
			propertyType = "String";
		}
				
		return propertyType;
	}
}
