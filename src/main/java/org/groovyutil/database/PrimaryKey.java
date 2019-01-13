package org.groovyutil.database;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PrimaryKey implements Serializable {
    public PrimaryKey() {
    }

    public String toString(Object obj) {
        return ToStringBuilder.reflectionToString(obj);
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, new String[0]);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, new String[0]);
    }
}

