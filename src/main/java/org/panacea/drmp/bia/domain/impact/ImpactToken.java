package org.panacea.drmp.bia.domain.impact;

import java.util.Objects;

public class ImpactToken {
    public Double value;
    public String sourceId;
    public int fragments; // how many fragments are there?
    public int fragId; // which fragments is this one?
    public ImpactToken originalToken;

    public ImpactToken(Double value, String sourceId, int fragments, int fragId, ImpactToken original) {
        this.value = value;
        this.sourceId = sourceId;
        this.fragments = fragments;
        this.fragId = fragId;
        this.originalToken = original;
    }

    public ImpactToken(Double value, String sourceId) {
        this(value, sourceId, 1, 1, null);
    }

    public ImpactToken clone() {
        return new ImpactToken(this.value, this.sourceId, this.fragments, this.fragId, this.originalToken);
    }

    @Override
    public String toString() {
        return "(" +
                "value=" + value +
                ", sourceId='" + sourceId + '\'' +
                ", fragments=" + fragments +
                ", fragId=" + fragId +
                ", originalToken=" + originalToken +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImpactToken that = (ImpactToken) o;
        return fragments == that.fragments &&
                fragId == that.fragId &&
                Objects.equals(value, that.value) &&
                Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(originalToken, that.originalToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, sourceId, fragments, fragId, originalToken);
    }
}
