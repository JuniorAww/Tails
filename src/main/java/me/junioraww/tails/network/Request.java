package me.junioraww.tails.network;

import javax.xml.transform.Result;
import java.util.Arrays;

public class Request {
    private final Action action;
    private final String arg;

    public Request(Action action, String arg) {
        this.action = action;
        this.arg = arg;
    }

    public static Request fromString(String s) {
        int separator = s.indexOf(':');
        String _action = s.substring(0, separator);
        Action action = Action.values()[Integer.parseInt(_action)];
        return new Request(action, s.substring(separator + 1));
    }

    @Override
    public String toString() {
        return action.id + ":" + String.join(":", arg);
    }

    public Action getAction() { return action; }
    public String getArgs() { return arg; }

    public enum Action {
        AUTH_0(0),
        AUTH_1(1),
        RESERVED_2(2),
        RESERVED_3(3),
        RESERVED_4(4),
        RESERVED_5(5),
        RESERVED_6(6),
        RESERVED_7(7),
        RESERVED_8(8),
        RESERVED_9(9),
        SYNC(10),
        ADD_TO_BALANCE(11),
        PAY_OTHER(12);

        private final int id;
        Action(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}

