package ch.ethz.inf.vs.lubu.cyrptdbmodule.crypto;

public class MessageUtils {
	
	public static final String MESSAGE_TOKEN_DELIM = "#";
	public static final String MESSAGE_DELIM = "\n";

	public enum MessageType {
		DECIDE_START ("DECIDE_START"),
		DECIDE_ORDER ("DECIDE_ORDER"),
		DECIDE_REPLY ("DECIDE_REPLY"),
		DECIDE_FOUND ("DECIDE_FOUND"),
		INSERT_SUCC ("INSERT_SUCC"),
		QUERY_RESULT ("QUERY_RESULT"),
		ERROR_CODE ("ERROR_CODE");
		
		private final String name;
		
		private MessageType(String s) {
			this.name = s;
		}
		
		public boolean compareTO(String other) {
			return name.equals(other);
		}
		
		public String toString() {
			return this.name;
		}
		
		 public static MessageType fromString(String text) {
		    if (text != null) {
		      for (MessageType b : MessageType.values()) {
		        if (text.equalsIgnoreCase(b.name)) {
		          return b;
		        }
		      }
		    }
		    return null;
		 }
	}
	
	
}
