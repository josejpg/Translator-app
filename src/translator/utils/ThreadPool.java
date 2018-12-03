/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translator.utils;

import java.util.*;
import java.util.concurrent.Callable;
import translator.model.Language;

/**
 *
 * @author Jose J. Pardines Garcia
 */
public class ThreadPool implements Callable<HashMap<String,HashMap<String,String>>>{
    
    private final HashMap<String, HashMap<String, String>> allTranslates = new HashMap<>();
    private final List<Language> listLang;
    
    /**
     * Constructor for ThreadPool with list of languages
     * @param listLang 
     */
    public ThreadPool(List<Language> listLang){
        this.listLang = listLang;
    }
    
    /**
     * Return all of translations hash maps
     * @return
     * @throws Exception 
     */
    public HashMap<String,HashMap<String,String>> call() throws Exception {
        listLang.forEach((dataLang) -> {
            allTranslates.put(dataLang.getSrcLang(), dataLang.getHashSentences());
        });
        
        return allTranslates;
    }
    
}
