package smallwheel.mybro.support.builder;
import org.jdom.DocType;
import org.jdom.Document;



/**
 * 
 * SqlMapperBuilder �߻�Ŭ����
 * 
 * @author yeonhooo
 *
 */
public abstract class SqlMapperBuilder {

	protected Document doc;
	protected DocType docType;
	protected String elementName;
	protected String publicID;
	protected String systemID;
	
	public abstract void writeSqlMap(String tableName);

}
