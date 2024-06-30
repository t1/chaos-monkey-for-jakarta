package de.codecentric.chaosmonkey.jakarta;

public record RestMethod(String name) {
    public static final RestMethod GET = RestMethod.of("GET");
    public static final RestMethod PUT = RestMethod.of("PUT");
    public static final RestMethod POST = RestMethod.of("POST");
    public static final RestMethod DELETE = RestMethod.of("DELETE");

    public static RestMethod of(String method) {return new RestMethod(method);}
}
