package io.apicurio.registry.model;

import jakarta.validation.ValidationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public class GroupId {

    /**
     * Pattern requirements:
     * - Must not contain reserved characters ":=,<>" (see VersionExpressionParser)
     * - Must fit in the database column
     */
    private static final Pattern VALID_PATTERN = Pattern.compile("[a-zA-Z0-9._-]{1,512}"); // TODO: UPGRADE INCOMPATIBILITY

    public static final GroupId DEFAULT = new GroupId();

    private final String rawGroupId;


    private GroupId() {
        // Skips validation
        this.rawGroupId = "__$GROUPID$__"; // TODO: Change this?
    }


    public GroupId(String rawGroupId) {
        if (!isValid(rawGroupId)) {
            throw new ValidationException("Group ID '" + rawGroupId + "' is invalid. " +
                    "It must consist of alphanumeric characters or '._-', and have length 1..512 (inclusive).");
        }
        this.rawGroupId = rawGroupId == null || "default".equalsIgnoreCase(rawGroupId) ? DEFAULT.getRawGroupId() : rawGroupId;
    }


    public boolean isDefaultGroup() {
        return DEFAULT.getRawGroupId().equals(rawGroupId);
    }


    public String getRawGroupIdWithDefault() {
        return isDefaultGroup() ? "default" : rawGroupId;
    }


    public String getRawGroupIdWithNull() {
        return isDefaultGroup() ? null : rawGroupId;
    }


    @Override
    public String toString() {
        return getRawGroupIdWithDefault();
    }


    public static boolean isValid(String rawGroupId) {
        return rawGroupId == null || DEFAULT.getRawGroupId().equals(rawGroupId) || VALID_PATTERN.matcher(rawGroupId).matches();
    }
}
