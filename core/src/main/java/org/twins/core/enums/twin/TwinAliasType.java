package org.twins.core.enums.twin;


public enum TwinAliasType {

    D, C, B, S, T, K;

    public static final String _D = "D"; // Domain key is uniq, so given type of alias is also uniq even between all registered domains
    public static final String _C = "C"; // Twin class key is uniq only in given domain. That is why such kind of alias can be duplicated between different domains.
    public static final String _B = "B"; // This alias is differ from domain class alias because it is uniq only inside BA inside some domain. Different BA can have same aliases in domain. For example PROJECT-1 in BA1 and PROJECT-1 in BA2 will refer to different twins.
//If some twin class was marked as alias_space then all twiins of given type should have uniq key (inside domain).
    public static final String _S = "S"; // Space for owner type domain
    public static final String _T = "T"; // Space for owner type domainBusinessAccount
    public static final String _K = "K"; // Space for owner type domainUser
}
