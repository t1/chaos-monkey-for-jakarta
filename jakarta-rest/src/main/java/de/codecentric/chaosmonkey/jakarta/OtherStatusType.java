package de.codecentric.chaosmonkey.jakarta;

import jakarta.ws.rs.core.Response;

class OtherStatusType implements Response.StatusType {
    private final int statusCode;

    public OtherStatusType(int statusCode) {this.statusCode = statusCode;}

    static Response.StatusType statusFromCode(int statusCode) {
        var status = (Response.StatusType) Response.Status.fromStatusCode(statusCode);
        if (status == null) status = new OtherStatusType(statusCode);
        return status;
    }

    @Override public int getStatusCode() {return statusCode;}

    @Override public Response.Status.Family getFamily() {return Response.Status.Family.familyOf(statusCode);}

    @Override public String getReasonPhrase() {return "Other Status Code";}

    @Override public String toString() {return "OTHER:" + statusCode;}
}
