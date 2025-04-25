package com.sinergise.java.cluster.swift;

import java.util.HashMap;
import java.util.Map;

import com.sinergise.common.cluster.swift.SwiftAccount;

public class SwiftAccountRepository {
    public static final SwiftAccountRepository INSTANCE=new SwiftAccountRepository();
    
    private Map<String, SwiftAccount> swiftAccuntsMap = new HashMap<String, SwiftAccount>();

    private SwiftAccount _getAccount(String accountName) {
    	return swiftAccuntsMap.get(accountName);
    }
    private void _addAccount(SwiftAccount account) {
    	if (account==null) return;
    	swiftAccuntsMap.put(account.getAccountName(), account);
    }
    
    public static SwiftAccount getAccount(String accountName) {
    	return INSTANCE._getAccount(accountName);
    }
    
    public static void addAccount(SwiftAccount account) {
    	INSTANCE._addAccount(account);
    }
}
