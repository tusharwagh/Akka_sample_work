package com.compare.model;

public class Message {

	private String uuid;
	
	private String message;
	
	private String corelationId;
	
	private boolean result;
	
	private boolean compared;
	
	private boolean isNull = false;
	
	public Message()
	{
		isNull = true;
	}
	
	public Message(String uuid, String message, String corelationId, boolean result, boolean compared)
	{
		this.uuid = uuid;
		this.message = message;
		this.corelationId = corelationId;
		this.result = result;
		this.compared = compared;
	}

	public String getUuid() {
		return uuid;
	}

	public String getMessage() {
		return message;
	}

	public String getCorelationId() {
		return corelationId;
	}

	public boolean isDone() {
		return result;
	}
	
	public boolean isCompared() {
		return compared;
	}	

	public boolean isNull() {
		return isNull;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Message [uuid=" + uuid + ", message=" + message
				+ ", corelationId=" + corelationId + ", result=" + result
				+ ", compared=" + compared + ", isNull=" + isNull + "]";
	}
}
