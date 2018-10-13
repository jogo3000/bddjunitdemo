package bddjunitdemo.audit;

public class Audit {
	private String actor;
	private String message;

	public Audit(String actor, String message) {
		this.actor = actor;
		this.message = message;
	}
	
	public String getActor() {
		return actor;
	}
	
	public String getMessage() {
		return message;
	}
}
