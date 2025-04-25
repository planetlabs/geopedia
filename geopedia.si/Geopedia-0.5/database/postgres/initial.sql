-- Revision
CREATE SEQUENCE seq_rev_num INCREMENT BY 1 MINVALUE 1 CACHE 1 NO CYCLE;
CREATE SEQUENCE seq_data_revision_id INCREMENT BY 1 MINVALUE 1 CACHE 1 NO CYCLE;

CREATE OR REPLACE FUNCTION "public"."start_transaction"()
  RETURNS void AS $BODY$
DECLARE
	-- declarations
	revision_id INTEGER;
BEGIN
	revision_id := current_setting('audit_vars.revision_id');
	IF revision_id != -1 THEN
		RAISE EXCEPTION 'Revision is not null! Previous transaction was not ended!';
	ELSE
		revision_id := nextval('public.seq_rev_num');
		PERFORM set_config('audit_vars.revision_id', ''||revision_id, false);
		PERFORM set_config('audit_vars.data_revision_id','-1', false);
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE COST 100;

ALTER FUNCTION "public"."start_transaction"() OWNER TO "geopedia";



CREATE OR REPLACE FUNCTION "public"."end_transaction"() 
	RETURNS void AS $BODY$
DECLARE
	-- declarations
BEGIN
	PERFORM set_config('audit_vars.revision_id', '-1', false);
	PERFORM set_config('audit_vars.data_revision_id','-1', false);
END;
$BODY$
	LANGUAGE 'plpgsql' VOLATILE COST 100;

CREATE OR REPLACE FUNCTION "public"."get_data_revision_id"() 
	RETURNS BIGINT AS $BODY$
DECLARE
	-- declarations
	L_CURRENT_REV_NUM BIGINT := current_setting('audit_vars.revision_id');
	L_CURRENT_DATA_REVISION BIGINT;
BEGIN
	   IF ( L_CURRENT_REV_NUM = -1) THEN RAISE EXCEPTION 'REV_NUM is null! Call public.START_TRANSACTION()!'; END IF;
	   L_CURRENT_DATA_REVISION := current_setting('audit_vars.data_revision_id');
	   IF (L_CURRENT_DATA_REVISION = -1) THEN
		L_CURRENT_DATA_REVISION := nextval('public.seq_data_revision_id');
		PERFORM set_config('audit_vars.data_revision_id',''||L_CURRENT_DATA_REVISION, false);	   
	   END IF;
	   return L_CURRENT_DATA_REVISION;
END;
$BODY$
	LANGUAGE 'plpgsql';

-- Metadata
-- Table
DROP SEQUENCE IF EXISTS metadata.seq_tables_table_id;
CREATE SEQUENCE metadata.seq_tables_table_id INCREMENT BY 1 MINVALUE 1 CACHE 1 NO CYCLE;



DROP TABLE IF EXISTS metadata.tables CASCADE;
CREATE TABLE metadata.tables (
  table_id bigint PRIMARY KEY  DEFAULT nextval('metadata.seq_tables_table_id') NOT NULL,
  table_name varchar(100) NOT NULL,
  table_desc text NOT NULL,
  table_styleJS text NOT NULL,
  table_geomtype smallint NOT NULL,
  table_lastdatawrite bigint default NULL,
  table_lastmetachange bigint default NULL,
  table_publicperms bigint NOT NULL,
  table_reptextJS varchar(255)  NOT NULL,
  table_deleted smallint NOT NULL,
  table_properties text default NULL,
  table_languages varchar(255)  default NULL,
  table_keywords varchar(255)  default NULL,
  table_minx DOUBLE PRECISION default NULL,
  table_miny DOUBLE PRECISION default NULL,
  table_maxx DOUBLE PRECISION default NULL,
  table_maxy DOUBLE PRECISION default NULL,
  jn_rev_num bigint default NULL
);


CREATE OR REPLACE FUNCTION audit.trg_tables_func() RETURNS TRIGGER AS $body$
DECLARE
	L_CURRENT_REV_NUM BIGINT := current_setting('audit_vars.revision_id');
	l_current_data_revision bigint;
BEGIN
   IF ( L_CURRENT_REV_NUM = -1) THEN RAISE EXCEPTION 'REV_NUM is null! Call public.START_TRANSACTION()!'; END IF;
   l_current_data_revision := get_data_revision_id();
   IF (TG_OP = 'INSERT') THEN
   		NEW.jn_rev_num := L_CURRENT_REV_NUM;
   		return NEW;
   END IF;
   		return NEW;
END;
$body$
	LANGUAGE 'plpgsql'

	
CREATE TRIGGER audit_tables BEFORE INSERT OR UPDATE OR DELETE
  ON metadata.tables FOR EACH ROW EXECUTE PROCEDURE audit.trg_tables_func();