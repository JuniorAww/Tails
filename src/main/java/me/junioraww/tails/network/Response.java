package me.junioraww.tails.network;

import java.util.Arrays;

public class Response {
    private final Result result;
    private final String arg;

    public Response(Result action, String arg) {
        this.result = action;
        this.arg = arg;
    }

    public static Response fromString(String s) {
        int separator = s.indexOf(':');
        String _result = s.substring(0, separator);
        Result result = Result.values()[Integer.parseInt(_result)];
        return new Response(result, s.substring(separator + 1));
    }

    @Override
    public String toString() {
        return result + ":" + arg;
    }

    public Result getResult() { return result; }
    public String getArg() { return arg; }

    public enum Result {
        SUCCESS(0),
        FAILURE(1),
        RESERVED_2(2),
        RESERVED_3(3),
        RESERVED_4(4),
        RESERVED_5(5),
        RESERVED_6(6),
        RESERVED_7(7),
        RESERVED_8(8),
        RESERVED_9(9),
        WALLET(10);

        private final int id;
        Result(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
