package group.amazcontacts.constant;

import androidx.annotation.NonNull;

public enum PhoneType {
    PRIMARY , SECONDARY, OTHER;

    @NonNull
    @Override
    public String toString() {
        switch (this){
            case PRIMARY:
                return "PRIMARY";
            case SECONDARY:
                return "SECONDARY";
            case OTHER:
                return "OTHER";
            default:
                break;
        }
        return super.toString();
    }
}
