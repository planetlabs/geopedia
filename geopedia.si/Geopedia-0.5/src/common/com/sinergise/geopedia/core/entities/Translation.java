package com.sinergise.geopedia.core.entities;

import java.util.HashMap;
import java.util.Map;

public class Translation {
    public static final class EntityId {
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EntityId other = (EntityId) obj;
            if (id != other.id)
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
        public EntityId(EntityType type, int id) {
            super();
            this.type = type;
            this.id = id;
        }
        EntityType type;
        int id;
        
    }
    
    public static final class KeyOnEntityIdentifier {
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            KeyOnEntityIdentifier other = (KeyOnEntityIdentifier) obj;
            if (id != other.id)
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }
        public KeyOnEntityIdentifier(Key key, int id) {
            super();
            this.key = key;
            this.id = id;
        }
        public final Key key;
        public final int id;
        
    }
        
    public static final class TranslationIdentifier {
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((entId == null) ? 0 : entId.hashCode());
            result = prime * result + ((language == null) ? 0 : language.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TranslationIdentifier other = (TranslationIdentifier) obj;
            if (entId == null) {
                if (other.entId != null)
                    return false;
            } else if (!entId.equals(other.entId))
                return false;
            if (language == null) {
                if (other.language != null)
                    return false;
            } else if (!language.equals(other.language))
                return false;
            return true;
        }

        public final KeyOnEntityIdentifier entId;
        public final Language language;

        public TranslationIdentifier(Key key, int id, Language language) {
            super();
            this.entId = new KeyOnEntityIdentifier(key, id);
            this.language = language;
        }
        
        
    } 
	
	public static final String SESSKEY_LANGUAGE = "language";
	public static final String SESSKEY_FILTER_BY_LANGUAGE = "filterByLanguage";
	
	public final TranslationIdentifier identifier;
	private String string;
	
	public Translation(int id, Key key, Language language, String string) {
		if (key==null || language==null) {
			throw new NullPointerException("Translation.Key and Translation.Language cannot be null.");
		}
		this.identifier = new TranslationIdentifier(key, id, language);
		this.string = string;
	}
	public Translation(int id, int key, String language, String string) {
		this(id, Key.get(key), Language.get(language), string);
	}
	public Translation(int id, int key, int language, String string) {
		this(id, Key.get(key), Language.get(language), string);
	}
	
	public int getId() {
	    return identifier.entId.id;
	}

	public Key getKey() {
	    return identifier.entId.key;
	}
	
	public KeyOnEntityIdentifier getEntityId() {
	    return identifier.entId;
	}
	
	public Language getLanguage() {
	    return identifier.language;
	}
	
	public String getString() { return string; }
	public void setString(String string) { this.string = string; }
	
	/**
	 * SI.id()=1, EN.id()=2, CZ.id()=3
	 * SI.code()="si", EN.code()="en", CZ.code="cz"
	 */
	public enum Language {
		
		SI, EN, CZ;
//		SI, EN, ME;
		public int key() { return this.ordinal() + 1; }
		public String code() { return this.name().toLowerCase(); }
		private static final Map<Integer, Language> map = new HashMap<Integer, Language>();
		static {
			Language[] values = Language.values();
			for (Language value : values) {
				map.put(value.key(), value);
			}
		}
		public static Language get(int language) {
			return map.get(language);
		}
		public static Language get(String language) {
			return Enum.valueOf(Language.class, language.toUpperCase());
		}
    }
	
	/**
	 * THEME_NAME.id()=1, THEME_DESC.id()=2, ..., FIELD_DESC.id()=6
	 */
	public enum Key {
		THEME_NAME, THEME_DESC, TABLE_NAME, TABLE_DESC, FIELD_NAME, FIELD_DESC;
		public int key() { return this.ordinal() + 1; }
		private static final Map<Integer, Key> map = new HashMap<Integer, Key>();
		static {
			Key[] values = Key.values();
			for (Key value : values) {
				map.put(value.key(), value);
			}
		}
		public static Key get(int id) {
			return map.get(id);
		}
    }
	
	/**
     * THEME.id()=1, TABLE.id()=2, FIELD.id()=3
     */
    public enum EntityType {
        THEME, TABLE, FIELD;
        public int entityType() { return this.ordinal() + 1; }
        private static final Map<Integer, EntityType> map = new HashMap<Integer, EntityType>();
        static {
            EntityType[] values = EntityType.values();
            for (EntityType value : values) {
                map.put(value.entityType(), value);
            }
        }
        public static EntityType get(int id) {
            return map.get(id);
        }
    }

	public boolean equals(Object obj) {
		if (obj==null) { return false; }
		if (! (obj instanceof Translation)) { return false; }
		return identifier.equals(((Translation)obj).identifier);
	}
	public boolean matches(Translation other) {
		return this.equals(other) && (this.string+"").equals(other.string+"");
	}
}
