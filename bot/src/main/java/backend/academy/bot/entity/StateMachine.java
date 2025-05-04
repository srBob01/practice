package backend.academy.bot.entity;

import static backend.academy.bot.entity.State.T_WAIT_TAG;

import org.springframework.stereotype.Component;

@Component
public class StateMachine {

    public State next(State state) {
        return switch (state) {
            case TRACK -> State.T_WAIT_LINK;
            case T_WAIT_LINK -> T_WAIT_TAG;
            case T_WAIT_TAG -> State.T_WAIT_FILTERS;
            case UNTRACK -> State.U_WAIT_LINK;
            case ADD_TAG -> State.A_WAIT_TAG;
            case DELETE_TAG -> State.D_WAIT_TAG;
            case LINKS_BY_TAG -> State.L_WAIT_TAG;
            case DELETE_LINKS_BY_TAG -> State.DL_WAIT_TAG;
            case SET_DAILY_DIGEST, SET_DIGEST_HOUR -> State.S_WAIT_HOUR;
            default -> State.HELP;
        };
    }

    public boolean isWait(State state) {
        return switch (state) {
            case TRACK,
                    T_WAIT_LINK,
                    T_WAIT_TAG,
                    T_WAIT_FILTERS,
                    UNTRACK,
                    U_WAIT_LINK,
                    ADD_TAG,
                    A_WAIT_TAG,
                    DELETE_TAG,
                    D_WAIT_TAG,
                    LINKS_BY_TAG,
                    L_WAIT_TAG,
                    DELETE_LINKS_BY_TAG,
                    DL_WAIT_TAG,
                    SET_DAILY_DIGEST,
                    SET_DIGEST_HOUR,
                    S_WAIT_HOUR -> true;
            default -> false;
        };
    }
}
