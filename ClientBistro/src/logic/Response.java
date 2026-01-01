package logic;

import java.io.Serializable;

public class Response implements Serializable {
    private String status;
    private Object data;

    public Response(String status, Object data) {
        this.status = status;
        this.data = data;
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object order) {
		this.data = order;
	}

    
}
