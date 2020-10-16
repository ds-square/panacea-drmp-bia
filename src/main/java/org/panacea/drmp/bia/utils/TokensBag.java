package org.panacea.drmp.bia.utils;

import org.panacea.drmp.bia.domain.impact.ImpactToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

public class TokensBag implements Iterable<ImpactToken> {
	public List<ImpactToken> tokens;

	public TokensBag() {
		this.tokens = new ArrayList<>();
	}

	public TokensBag(List<ImpactToken> tokens) {
		this.tokens = tokens;
	}

	public TokensBag clone() {
		List<ImpactToken> clonedTokens = new ArrayList<>();
		for (ImpactToken t : this.tokens) {
			clonedTokens.add(t.clone());
		}
		return new TokensBag(clonedTokens);
	}

	public void add(ImpactToken token) {
		if (this.tokens.contains(token)) return;
		ImpactToken parent = token.originalToken;
		if (parent != null) {
			// check if there are all the fragments and defrag in case
			List<ImpactToken> fragments = new ArrayList<>();
			for (ImpactToken t : this.tokens) {
				if (t.originalToken != null && t.originalToken.equals(parent)) {
					fragments.add(t);
				}
			}
			if ((fragments.size() + 1) == token.fragments) {
				// here I could check if there are k over n nodes
				this.tokens.removeAll(fragments);
				this.add(parent);
				return;
			}
		}
		this.tokens.add(token);
	}

	public void addAll(TokensBag tb) {
		for (ImpactToken t : tb.tokens) {
			this.add(t);
		}
	}

	public TokensBag split(int fragId, int fragments) {
		List<ImpactToken> newTokens = new ArrayList<>();
		ImpactToken splitToken;
		for (ImpactToken t : this.tokens) {
			splitToken = t.clone();
			splitToken.fragId = fragId;
			splitToken.fragments = fragments;
			splitToken.originalToken = t;
			newTokens.add(splitToken);
		}
		return new TokensBag(newTokens);
	}

	@Override
	public String toString() {
		StringJoiner result = new StringJoiner("; ");
		for (ImpactToken t : this.tokens) {
			result.add(t.toString());
		}
		return "TokensBag{" + result.toString() + "}";
	}

	@Override
	public Iterator<ImpactToken> iterator() {
		return this.tokens.iterator();
	}
}
