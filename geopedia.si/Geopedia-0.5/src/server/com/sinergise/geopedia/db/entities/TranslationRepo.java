package com.sinergise.geopedia.db.entities;

import static com.sinergise.geopedia.core.entities.Translation.EntityType.FIELD;
import static com.sinergise.geopedia.core.entities.Translation.EntityType.TABLE;
import static com.sinergise.geopedia.core.entities.Translation.EntityType.THEME;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.sinergise.geopedia.core.entities.Translation;
import com.sinergise.geopedia.core.entities.Translation.EntityId;
import com.sinergise.geopedia.core.entities.Translation.Key;
import com.sinergise.geopedia.core.entities.Translation.KeyOnEntityIdentifier;
import com.sinergise.geopedia.core.entities.Translation.Language;
import com.sinergise.geopedia.core.entities.Translation.TranslationIdentifier;
import com.sinergise.util.collections.Predicate;

/**
 * TranslationRepo represents cache of "translations" and "languages".
 * "translations" are backed by pedicamain.translations table and represent a translation for a specific {@link Translation.Key} in a specific {@link Translation.Language} 
 * "languages" (theme_languages, table_languages and field_languages) are backed by pedicamain.themes/tables/and fields respectively.
 * "languages" may be used as a filter (i.e.: Let me see and search only those themes and layers which meta data have been translated into English)
 * 
 * @author amarolt
 */
public class TranslationRepo {
	
	private TranslationRepo() {
		System.out.println("TranslationRepo()");
		INSTANCE = TranslationRepo.this;
		clear();
	}
	
	private static TranslationRepo INSTANCE = null;
    public static TranslationRepo getRepository() {
    	if (INSTANCE==null) {
    		INSTANCE = new TranslationRepo();
    	}
    	return INSTANCE;
    }

    // Contains all translations of texts, identified by the KeyOnEntityIdentifier
	private final Map<KeyOnEntityIdentifier, Map<Language, Translation>> mapId = new HashMap<KeyOnEntityIdentifier, Map<Language, Translation>>(1024);
	
	// Contains list of entity availability in languages (an entity will be displayed if its languages map contains the language in question)
    private final Map<EntityId, Collection<Language>> languagesMap = new HashMap<EntityId, Collection<Language>>(0);
        
	public Map<Language, Translation> getTranslationsFor(KeyOnEntityIdentifier id) {
	    return mapId.get(id);
	}
	
    public void removeFromCache(Translation translation) {
        Map<Language, Translation> map = getTranslationsFor(translation.getEntityId());
        if (map==null) return;

        map.remove(translation.getLanguage());
   	    
        if (map.isEmpty()) {
            mapId.remove(translation.getEntityId());
        }
    }
    
    public void writeToCache(Translation translation) {
        Map<Language, Translation> map = getTranslationsFor(translation.getEntityId());        
        if (map == null) map = new HashMap<Language, Translation>();
    	map.put(translation.getLanguage(), translation);
    }
    
    public Translation search(int id, Key key, Language language) {
        Map<Language, Translation> map = getTranslationsFor(new KeyOnEntityIdentifier(key, id));
        if (map == null) return null;
        return map.get(language);
    }
    
    public Translation get(TranslationIdentifier identifier) {
        Map<Language, Translation> map = getTranslationsFor(identifier.entId);
        if (map == null) return null;
        return map.get(identifier.language);
    }
    
    private Map<Language, String> filter(int id, Key key) {
        Map<Language, Translation> map =  getTranslationsFor(new KeyOnEntityIdentifier(key, id));
        if (map == null) {
            return new HashMap<Language, String>();
        }
        Map<Language, String> retMap = new HashMap<Language, String>(map.size());
        for (Translation tr: map.values()) {
            retMap.put(tr.getLanguage(), tr.getString());
        }
        return retMap;
    }
    public Map<Language, String> themeName(int id) {
    	return this.filter(id, Key.THEME_NAME);
    }
    public Map<Language, String> themeDesc(int id) {
    	return this.filter(id, Key.THEME_DESC);
    }
    public Map<Language, String> tableName(int id) {
    	return this.filter(id, Key.TABLE_NAME);
    }
    public Map<Language, String> tableDesc(int id) {
    	return this.filter(id, Key.TABLE_DESC);
    }
    public Map<Language, String> fieldName(int id) {
    	return this.filter(id, Key.FIELD_NAME);
    }
    public Map<Language, String> fieldDesc(int id) {
    	return this.filter(id, Key.FIELD_DESC);
    }
	
	private Collection<Integer> find(Key nameKey, Key descKey, Language language, Predicate<String> filter) {
		HashSet<Integer> result = new HashSet<Integer>();
		HashMap<Integer, String> temp = new HashMap<Integer, String>();
		
		for (Map.Entry<KeyOnEntityIdentifier, Map<Language, Translation>> mapE: mapId.entrySet()) {
		    KeyOnEntityIdentifier entIdent = mapE.getKey();
		    Key entKey = entIdent.key;  
		    if (!(entKey.equals(nameKey) || entKey.equals(descKey))) continue;
            if (result.contains(entIdent.id)) continue;
		    
		    Map<Language, Translation> entMap = mapE.getValue();
            String curStr = entMap.get(language).getString();
            if (curStr==null) curStr="";

		    if (filter.eval(curStr)) {
		        // Maybe the name or description fits the filter on its own, without concatenating both together
                result.add(entIdent.id);
            } else {
                // Try to concatenate the name and description
                String prevString = temp.get(entIdent.id);
                if (prevString == null) {
                    // We haven't saved the other one yet save this and wait for the other part
                    temp.put(entIdent.id, curStr);
                } else {
                    // The previous part has been saved, we can now concatenate and test the filter
                    if (filter.eval(prevString+" "+curStr)) {
                        result.add(entIdent.id);
                    }
                    // We won't need the stored part any more
                    temp.remove(entIdent.id);
                }
            }
        }
		return result;
	}
	public Integer[] findThemes(Language language, Predicate<String> filter) {
		Collection<Integer> themeIds = find(Key.THEME_NAME, Key.THEME_DESC, language, filter);
		return themeIds.toArray(new Integer[themeIds.size()]);
	}
	public Integer[] findTables(Language language, Predicate<String> filter) {
		Collection<Integer> tableIds = find(Key.TABLE_NAME, Key.TABLE_DESC, language, filter);
		return tableIds.toArray(new Integer[tableIds.size()]);
	}
	

	public void commitThemeLanguages(int themeId, Collection<Language> languages) {
		HashSet<Language> languageSet = new HashSet<Language>(languages);
		languagesMap.put(new EntityId(THEME, themeId), languageSet);
	}
	public void commitTableLanguages(int tableId, Collection<Language> languages) {
		HashSet<Language> languageSet = new HashSet<Language>(languages);
        languagesMap.put(new EntityId(TABLE, tableId), languageSet);
	}
	public void commitFieldLanguages(int fieldId, Collection<Language> languages) {
        HashSet<Language> languageSet = new HashSet<Language>(languages);
        languagesMap.put(new EntityId(FIELD, fieldId), languageSet);
	}
	public Collection<Language> readThemeLanguages(int themeId) {
	    return languagesMap.get(new EntityId(THEME, themeId));
    }
	public Collection<Language> readTableLanguages(int tableId) {
	    return languagesMap.get(new EntityId(TABLE, tableId));
	}
	public Collection<Language> readFieldLanguages(int fieldId) {
	    return languagesMap.get(new EntityId(FIELD, fieldId));
	}
	
	public boolean themeContains(int themeId, Language language) {
	    Collection<Language> languageSet = readThemeLanguages(themeId);
	    return languageSet.contains(language);
	}
	public boolean tableContains(int tableId, Language language) {
	    Collection<Language> languageSet = readTableLanguages(tableId);
        return languageSet.contains(language);
	}
	public boolean fieldContains(int fieldId, Language language) {
	    Collection<Language> languageSet = readFieldLanguages(fieldId);
        return languageSet.contains(language);
	}
	
	
	private void clear() {
    	mapId.clear();
    	languagesMap.clear();
    }
	
	public void deleteTheme(int themeId) {
	    EntityId entityId = new EntityId(THEME, themeId);
	    if (languagesMap.containsKey(entityId)) {
	        Collection<Language> languages = languagesMap.get(entityId);
            if (languages != null && languages.size() > 0) {
                for (Language language: languages) {
                    {
                        Translation translation = search(themeId, Key.THEME_NAME, language);
                        if (translation != null) {
                            removeFromCache(translation);
                        }
                    }
                    {
                        Translation translation = search(themeId, Key.THEME_DESC, language);
                        if (translation != null) {
                            removeFromCache(translation);
                        }
                    }
                }
            }
            languagesMap.remove(entityId);
	    }
	}
	
	public void deleteTable(int tableId) {
	    EntityId entityId = new EntityId(TABLE, tableId);
        if (languagesMap.containsKey(entityId)) {
			Collection<Language> languages = languagesMap.get(entityId);
			if (languages!=null && languages.size()>0) {
				for (Language language : languages) {
					{
						Translation translation = search(tableId, Key.TABLE_NAME, language);
						if (translation!=null) {
							removeFromCache(translation);
						}
					}
					{
						Translation translation = search(tableId, Key.TABLE_DESC, language);
						if (translation!=null) {
							removeFromCache(translation);
						}
					}
				}
			}
			languagesMap.remove(entityId);
		}
	}
	
	public void deleteField(int fieldId) {
	    EntityId entityId = new EntityId(FIELD, fieldId);
        if (languagesMap.containsKey(entityId)) {
			Collection<Language> languages = languagesMap.get(entityId);
			if (languages!=null && languages.size()>0) {
				for (Language language : languages) {
					{
						Translation translation = search(fieldId, Key.FIELD_NAME, language);
						if (translation!=null) {
							removeFromCache(translation);
						}
					}
					{
						Translation translation = search(fieldId, Key.FIELD_DESC, language);
						if (translation!=null) {
							removeFromCache(translation);
						}
					}
				}
			}
			languagesMap.remove(entityId);
		}
	}
}
