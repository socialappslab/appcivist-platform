-- ----------------------------------------------------------------------------
-- MySQL Workbench Migration
-- Migrated Schemata: appcivistcore2
-- Source Schemata: appcivistcore2
-- Created: Sat Aug 20 21:25:17 2016
-- Workbench Version: 6.3.6
-- ----------------------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- Schema appcivistcore2
-- ----------------------------------------------------------------------------
DROP SCHEMA IF EXISTS `appcivistcore2` ;
CREATE SCHEMA IF NOT EXISTS `appcivistcore2` ;

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.appcivist_user
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`appcivist_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `uuid` VARCHAR(40) NULL,
  `email` VARCHAR(255) NULL,
  `name` VARCHAR(255) NULL,
  `username` VARCHAR(255) NULL,
  `language` VARCHAR(255) NULL,
  `email_verified` TINYINT NULL,
  `profile_pic_resource_id` BIGINT NULL,
  `active` TINYINT NULL,
  PRIMARY KEY (`user_id`),
  INDEX `ix_appcivist_user_profilepic_34` (`profile_pic_resource_id` ASC),
  UNIQUE INDEX `uq_appcivist_user_profile_pic_re` (`profile_pic_resource_id` ASC),
  CONSTRAINT `fk_appcivist_user_profilepic_34`
    FOREIGN KEY (`profile_pic_resource_id`)
    REFERENCES `appcivistcore2`.`resource` (`resource_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.assembly
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`assembly` (
  `assembly_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  `name` VARCHAR(255) NULL,
  `shortname` VARCHAR(255) NULL,
  `description` LONGTEXT NULL,
  `url` VARCHAR(255) NULL,
  `listed` TINYINT NULL,
  `invitationemail` LONGTEXT NULL,
  `profile_assembly_profile_id` BIGINT NULL,
  `location_location_id` BIGINT NULL,
  `resources_resource_space_id` BIGINT NULL,
  `forum_resource_space_id` BIGINT NULL,
  `creator_user_id` BIGINT NULL,
  PRIMARY KEY (`assembly_id`),
  INDEX `ix_assembly_creator_5` (`creator_user_id` ASC),
  INDEX `ix_assembly_forum_4` (`forum_resource_space_id` ASC),
  INDEX `ix_assembly_location_2` (`location_location_id` ASC),
  INDEX `ix_assembly_profile_1` (`profile_assembly_profile_id` ASC),
  INDEX `ix_assembly_resources_3` (`resources_resource_space_id` ASC),
  INDEX `ix_assembly_uuid_39` (`uuid` ASC),
  UNIQUE INDEX `uq_assembly_forum_resource_space` (`forum_resource_space_id` ASC),
  UNIQUE INDEX `uq_assembly_profile_assembly_pro` (`profile_assembly_profile_id` ASC),
  UNIQUE INDEX `uq_assembly_resources_resource_s` (`resources_resource_space_id` ASC),
  CONSTRAINT `fk_assembly_creator_5`
    FOREIGN KEY (`creator_user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_assembly_forum_4`
    FOREIGN KEY (`forum_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_assembly_location_2`
    FOREIGN KEY (`location_location_id`)
    REFERENCES `appcivistcore2`.`location` (`location_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_assembly_profile_1`
    FOREIGN KEY (`profile_assembly_profile_id`)
    REFERENCES `appcivistcore2`.`assembly_profile` (`assembly_profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_assembly_resources_3`
    FOREIGN KEY (`resources_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.assembly_profile
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`assembly_profile` (
  `assembly_profile_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `target_audience` VARCHAR(255) NULL,
  `supported_membership` VARCHAR(22) NULL,
  `management_type` VARCHAR(25) NULL,
  `icon` VARCHAR(255) NULL,
  `cover` VARCHAR(255) NULL,
  `primary_contact_name` VARCHAR(255) NULL,
  `primary_contact_phone` VARCHAR(255) NULL,
  `primary_contact_email` VARCHAR(255) NULL,
  PRIMARY KEY (`assembly_profile_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.ballot
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`ballot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `uuid` VARCHAR(40) NULL,
  `password` VARCHAR(255) NULL,
  `instructions` LONGTEXT NULL,
  `notes` LONGTEXT NULL,
  `voting_system_type` VARCHAR(11) NULL,
  `starts_at` DATETIME NULL,
  `ends_at` DATETIME NULL,
  `created_at` DATETIME NULL,
  `updated_at` DATETIME NULL,
  `removed` TINYINT NULL,
  `removed_at` DATETIME NULL,
  `require_registration` TINYINT NULL,
  `user_uuid_as_signature` TINYINT NULL,
  `decision_type` VARCHAR(40) NULL,
  `component` BIGINT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_ballot_id_50` (`id` ASC))
AUTO_INCREMENT = 9000;

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.ballot_configuration
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`ballot_configuration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ballot_id` BIGINT NULL,
  `key` LONGTEXT NULL,
  `value` LONGTEXT NULL,
  `position` INT NULL,
  `created_at` DATETIME NULL,
  `updated_at` DATETIME NULL,
  `removed_at` DATETIME NULL,
  `removed` TINYINT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_ballot_configuration_key` (`key`(255) ASC),
  CONSTRAINT `fk_ballot_config_ballot`
    FOREIGN KEY (`ballot_id`)
    REFERENCES `appcivistcore2`.`ballot` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
AUTO_INCREMENT = 9000;

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.ballot_paper
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`ballot_paper` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ballot_id` BIGINT NULL,
  `uuid` LONGTEXT NULL,
  `signature` LONGTEXT NULL,
  `status` INT NULL,
  `created_at` DATETIME NULL,
  `updated_at` DATETIME NULL,
  `removed_at` DATETIME NULL,
  `removed` TINYINT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_ballot_paper_signature` (`signature`(255) ASC),
  CONSTRAINT `fk_ballot_paper_ballot`
    FOREIGN KEY (`ballot_id`)
    REFERENCES `appcivistcore2`.`ballot` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
AUTO_INCREMENT = 9000;

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.ballot_registration_field
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`ballot_registration_field` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ballot_id` BIGINT NULL,
  `name` VARCHAR(255) NULL,
  `description` LONGTEXT NULL,
  `expected_value` LONGTEXT NULL,
  `position` INT NULL,
  `removed` TINYINT NULL,
  `removed_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_ballot_registration_field__49` (`id` ASC),
  CONSTRAINT `fk_registration_field_ballot`
    FOREIGN KEY (`ballot_id`)
    REFERENCES `appcivistcore2`.`ballot` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
AUTO_INCREMENT = 9000;

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.campaign
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`campaign` (
  `campaign_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `title` VARCHAR(255) NULL,
  `shortname` VARCHAR(255) NULL,
  `goal` LONGTEXT NULL,
  `url` VARCHAR(255) NULL,
  `uuid` VARCHAR(40) NULL,
  `listed` TINYINT NULL,
  `resources_resource_space_id` BIGINT NULL,
  `template_campaign_template_id` BIGINT NULL,
  `consultive_ballot` VARCHAR(40) NULL,
  `binding_ballot` VARCHAR(40) NULL,
  PRIMARY KEY (`campaign_id`),
  INDEX `ix_campaign_resources_6` (`resources_resource_space_id` ASC),
  INDEX `ix_campaign_template_7` (`template_campaign_template_id` ASC),
  UNIQUE INDEX `uq_campaign_resources_resource_s` (`resources_resource_space_id` ASC),
  CONSTRAINT `fk_campaign_resources_6`
    FOREIGN KEY (`resources_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_campaign_template_7`
    FOREIGN KEY (`template_campaign_template_id`)
    REFERENCES `appcivistcore2`.`campaign_template` (`campaign_template_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.campaign_required_configuration
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`campaign_required_configuration` (
  `campaign_required_configuration_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  `campaign_template_campaign_template_id` BIGINT NULL,
  `config_definition_uuid` VARCHAR(40) NULL,
  PRIMARY KEY (`campaign_required_configuration_id`),
  INDEX `ix_campaign_required_configura_8` (`campaign_template_campaign_template_id` ASC),
  INDEX `ix_campaign_required_configura_9` (`config_definition_uuid` ASC),
  CONSTRAINT `fk_campaign_required_configura_8`
    FOREIGN KEY (`campaign_template_campaign_template_id`)
    REFERENCES `appcivistcore2`.`campaign_template` (`campaign_template_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_campaign_required_configura_9`
    FOREIGN KEY (`config_definition_uuid`)
    REFERENCES `appcivistcore2`.`config_definition` (`uuid`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.campaign_template
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`campaign_template` (
  `campaign_template_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `name_key` VARCHAR(23) NULL,
  `name` VARCHAR(255) NULL,
  PRIMARY KEY (`campaign_template_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.campaign_template_def_components
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`campaign_template_def_components` (
  `campaign_template_campaign_template_id` BIGINT NOT NULL,
  `component_definition_component_def_id` BIGINT NOT NULL,
  PRIMARY KEY (`campaign_template_campaign_template_id`, `component_definition_component_def_id`),
  CONSTRAINT `fk_campaign_template_def_comp_01`
    FOREIGN KEY (`campaign_template_campaign_template_id`)
    REFERENCES `appcivistcore2`.`campaign_template` (`campaign_template_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_campaign_template_def_comp_02`
    FOREIGN KEY (`component_definition_component_def_id`)
    REFERENCES `appcivistcore2`.`component_definition` (`component_def_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.campaign_template_req_configs
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`campaign_template_req_configs` (
  `campaign_template_campaign_template_id` BIGINT NOT NULL,
  `campaign_required_configuration_campaign_required_configuration` BIGINT NOT NULL,
  PRIMARY KEY (`campaign_template_campaign_template_id`, `campaign_required_configuration_campaign_required_configuration`),
  CONSTRAINT `fk_campaign_template_req_conf_01`
    FOREIGN KEY (`campaign_template_campaign_template_id`)
    REFERENCES `appcivistcore2`.`campaign_template` (`campaign_template_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_campaign_template_req_conf_02`
    FOREIGN KEY (`campaign_required_configuration_campaign_required_configuration`)
    REFERENCES `appcivistcore2`.`campaign_required_configuration` (`campaign_required_configuration_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.campaign_timeline_edge
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`campaign_timeline_edge` (
  `edge_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `campaign_campaign_id` BIGINT NULL,
  `start` TINYINT NULL,
  `from_component_component_id` BIGINT NULL,
  `to_component_component_id` BIGINT NULL,
  PRIMARY KEY (`edge_id`),
  INDEX `ix_campaign_timeline_edge_cam_10` (`campaign_campaign_id` ASC),
  INDEX `ix_campaign_timeline_edge_fro_11` (`from_component_component_id` ASC),
  INDEX `ix_campaign_timeline_edge_toc_12` (`to_component_component_id` ASC),
  CONSTRAINT `fk_campaign_timeline_edge_cam_10`
    FOREIGN KEY (`campaign_campaign_id`)
    REFERENCES `appcivistcore2`.`campaign` (`campaign_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_campaign_timeline_edge_fro_11`
    FOREIGN KEY (`from_component_component_id`)
    REFERENCES `appcivistcore2`.`component` (`component_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_campaign_timeline_edge_toc_12`
    FOREIGN KEY (`to_component_component_id`)
    REFERENCES `appcivistcore2`.`component` (`component_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.candidate
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`candidate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ballot_id` BIGINT NULL,
  `uuid` LONGTEXT NULL,
  `candidate_type` INT NULL,
  `contribution_uuid` LONGTEXT NULL,
  `created_at` DATETIME NULL,
  `updated_at` DATETIME NULL,
  `removed_at` DATETIME NULL,
  `removed` TINYINT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_candidate_uuid` (`uuid`(255) ASC),
  CONSTRAINT `fk_candidate_ballot`
    FOREIGN KEY (`ballot_id`)
    REFERENCES `appcivistcore2`.`ballot` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
AUTO_INCREMENT = 9000;

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.component
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`component` (
  `component_id` BIGINT NOT NULL,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `title` VARCHAR(255) NULL,
  `key` VARCHAR(255) NULL,
  `description` LONGTEXT NULL,
  `start_date` DATETIME NULL,
  `end_date` DATETIME NULL,
  `uuid` VARCHAR(40) NULL,
  `position` INT NULL,
  `timeline` INT NULL,
  `definition_component_def_id` BIGINT NULL,
  `resource_space_resource_space_id` BIGINT NULL,
  PRIMARY KEY (`component_id`),
  INDEX `ix_component_definition_13` (`definition_component_def_id` ASC),
  INDEX `ix_component_resourcespace_14` (`resource_space_resource_space_id` ASC),
  UNIQUE INDEX `uq_component_resource_space_reso` (`resource_space_resource_space_id` ASC),
  CONSTRAINT `fk_component_definition_13`
    FOREIGN KEY (`definition_component_def_id`)
    REFERENCES `appcivistcore2`.`component_definition` (`component_def_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_component_resourcespace_14`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.component_definition
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`component_definition` (
  `component_def_id` BIGINT NOT NULL,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  `name` VARCHAR(255) NULL,
  `description` LONGTEXT NULL,
  PRIMARY KEY (`component_def_id`),
  INDEX `ix_component_definition_uuid_40` (`uuid` ASC));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.component_milestone
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`component_milestone` (
  `component_milestone_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `title` VARCHAR(255) NULL,
  `key` VARCHAR(255) NULL,
  `position` INT NULL,
  `description` LONGTEXT NULL,
  `date` DATETIME NULL,
  `days` INT NULL,
  `uuid` VARCHAR(40) NULL,
  `type` VARCHAR(8) NULL,
  `main_contribution_type` INT NULL,
  PRIMARY KEY (`component_milestone_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.component_required_configuration
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`component_required_configuration` (
  `component_required_configuration_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `component_def_component_def_id` BIGINT NULL,
  `config_def_uuid` VARCHAR(40) NULL,
  PRIMARY KEY (`component_required_configuration_id`),
  INDEX `ix_component_required_configu_15` (`component_def_component_def_id` ASC),
  INDEX `ix_component_required_configu_16` (`config_def_uuid` ASC),
  CONSTRAINT `fk_component_required_configu_15`
    FOREIGN KEY (`component_def_component_def_id`)
    REFERENCES `appcivistcore2`.`component_definition` (`component_def_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_component_required_configu_16`
    FOREIGN KEY (`config_def_uuid`)
    REFERENCES `appcivistcore2`.`config_definition` (`uuid`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.component_required_milestone
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`component_required_milestone` (
  `component_required_milestone_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `title` VARCHAR(255) NULL,
  `description` LONGTEXT NULL,
  `key` VARCHAR(255) NULL,
  `position` INT NULL,
  `no_duration` TINYINT NULL,
  `type` VARCHAR(8) NULL,
  `target_component_uuid` VARCHAR(40) NULL,
  `campaign_template_campaign_template_id` BIGINT NULL,
  PRIMARY KEY (`component_required_milestone_id`),
  INDEX `ix_component_required_milesto_17` (`campaign_template_campaign_template_id` ASC),
  INDEX `ix_component_required_milesto_41` (`target_component_uuid` ASC),
  CONSTRAINT `fk_component_required_milesto_17`
    FOREIGN KEY (`campaign_template_campaign_template_id`)
    REFERENCES `appcivistcore2`.`campaign_template` (`campaign_template_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.config
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`config` (
  `uuid` VARCHAR(40) NOT NULL,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `key` VARCHAR(255) NULL,
  `value` LONGTEXT NULL,
  `config_target` VARCHAR(13) NULL,
  `target_uuid` VARCHAR(40) NULL,
  `definition_uuid` VARCHAR(40) NULL,
  PRIMARY KEY (`uuid`),
  INDEX `ix_config_definition_18` (`definition_uuid` ASC),
  CONSTRAINT `fk_config_definition_18`
    FOREIGN KEY (`definition_uuid`)
    REFERENCES `appcivistcore2`.`config_definition` (`uuid`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.config_definition
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`config_definition` (
  `uuid` VARCHAR(40) NOT NULL,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `key` VARCHAR(255) NULL,
  `value_type` VARCHAR(255) NULL,
  `description` LONGTEXT NULL,
  `default_value` VARCHAR(255) NULL,
  `config_target` VARCHAR(13) NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE INDEX `uq_config_definition_1` (`key` ASC));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.contribution
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`contribution` (
  `contribution_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  `title` VARCHAR(255) NULL,
  `text` LONGTEXT NULL,
  `type` INT NULL,
  `text_index` LONGTEXT NULL,
  `location_location_id` BIGINT NULL,
  `budget` VARCHAR(255) NULL,
  `resource_space_resource_space_id` BIGINT NULL,
  `stats_contribution_statistics_id` BIGINT NULL,
  `action_due_date` DATETIME NULL,
  `action_done` TINYINT NULL,
  `action` VARCHAR(255) NULL,
  `assessment_summary` VARCHAR(255) NULL,
  `extended_text_pad_resource_id` BIGINT NULL,
  PRIMARY KEY (`contribution_id`),
  INDEX `ix_contribution_extendedtextp_22` (`extended_text_pad_resource_id` ASC),
  INDEX `ix_contribution_location_19` (`location_location_id` ASC),
  INDEX `ix_contribution_resourcespace_20` (`resource_space_resource_space_id` ASC),
  INDEX `ix_contribution_stats_21` (`stats_contribution_statistics_id` ASC),
  INDEX `ix_contribution_text_index_43` (`text_index`(255) ASC),
  INDEX `ix_contribution_uuid_42` (`uuid` ASC),
  UNIQUE INDEX `uq_contribution_extended_text_pa` (`extended_text_pad_resource_id` ASC),
  UNIQUE INDEX `uq_contribution_location_locatio` (`location_location_id` ASC),
  UNIQUE INDEX `uq_contribution_resource_space_r` (`resource_space_resource_space_id` ASC),
  UNIQUE INDEX `uq_contribution_stats_contributi` (`stats_contribution_statistics_id` ASC),
  CONSTRAINT `fk_contribution_extendedtextp_22`
    FOREIGN KEY (`extended_text_pad_resource_id`)
    REFERENCES `appcivistcore2`.`resource` (`resource_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_contribution_location_19`
    FOREIGN KEY (`location_location_id`)
    REFERENCES `appcivistcore2`.`location` (`location_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_contribution_resourcespace_20`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_contribution_stats_21`
    FOREIGN KEY (`stats_contribution_statistics_id`)
    REFERENCES `appcivistcore2`.`contribution_statistics` (`contribution_statistics_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.contribution_appcivist_user
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`contribution_appcivist_user` (
  `contribution_contribution_id` BIGINT NOT NULL,
  `appcivist_user_user_id` BIGINT NOT NULL,
  PRIMARY KEY (`contribution_contribution_id`, `appcivist_user_user_id`),
  CONSTRAINT `fk_contribution_appcivist_use_01`
    FOREIGN KEY (`contribution_contribution_id`)
    REFERENCES `appcivistcore2`.`contribution` (`contribution_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_contribution_appcivist_use_02`
    FOREIGN KEY (`appcivist_user_user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.contribution_feedback
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`contribution_feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `user_id` BIGINT NOT NULL,
  `contribution_id` BIGINT NOT NULL,
  `up` TINYINT NULL,
  `down` TINYINT NULL,
  `fav` TINYINT NULL,
  `flag` TINYINT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_contribution_feedback_contribution`
    FOREIGN KEY (`contribution_id`)
    REFERENCES `appcivistcore2`.`contribution` (`contribution_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_contribution_feedback_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.contribution_statistics
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`contribution_statistics` (
  `contribution_statistics_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `ups` BIGINT NULL,
  `downs` BIGINT NULL,
  `favs` BIGINT NULL,
  `views` BIGINT NULL,
  `replies` BIGINT NULL,
  `flags` BIGINT NULL,
  `shares` BIGINT NULL,
  PRIMARY KEY (`contribution_statistics_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.contribution_template
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`contribution_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_contribution_template_uuid_44` (`uuid` ASC));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.contribution_template_section
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`contribution_template_section` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `contribution_template_id` BIGINT NOT NULL,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  `title` VARCHAR(255) NULL,
  `description` LONGTEXT NULL,
  `length` INT NULL,
  `position` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_contribution_template_sect_23` (`contribution_template_id` ASC),
  INDEX `ix_contribution_template_sect_45` (`uuid` ASC),
  CONSTRAINT `fk_contribution_template_sect_23`
    FOREIGN KEY (`contribution_template_id`)
    REFERENCES `appcivistcore2`.`contribution_template` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.geo
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`geo` (
  `location_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `type` VARCHAR(255) NULL,
  PRIMARY KEY (`location_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.geometry
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`geometry` (
  `geometry_id` BIGINT NOT NULL AUTO_INCREMENT,
  `type` INT NULL,
  `coordinates` VARCHAR(255) NULL,
  `geo_location_id` BIGINT NULL,
  PRIMARY KEY (`geometry_id`),
  INDEX `ix_geometry_geo_24` (`geo_location_id` ASC),
  CONSTRAINT `fk_geometry_geo_24`
    FOREIGN KEY (`geo_location_id`)
    REFERENCES `appcivistcore2`.`geo` (`location_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.hashtag
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`hashtag` (
  `hashtag_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `hashtag` VARCHAR(255) NULL,
  PRIMARY KEY (`hashtag_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.initial_data_config
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`initial_data_config` (
  `data_file_id` BIGINT NOT NULL AUTO_INCREMENT,
  `data_file` VARCHAR(255) NULL,
  `loaded` TINYINT NULL,
  PRIMARY KEY (`data_file_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.linked_account
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`linked_account` (
  `account_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NULL,
  `provider_user_id` VARCHAR(255) NULL,
  `provider_key` VARCHAR(255) NULL,
  PRIMARY KEY (`account_id`),
  INDEX `ix_linked_account_user_25` (`user_id` ASC),
  CONSTRAINT `fk_linked_account_user_25`
    FOREIGN KEY (`user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.location
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`location` (
  `location_id` BIGINT NOT NULL AUTO_INCREMENT,
  `place_name` VARCHAR(255) NULL,
  `street` VARCHAR(255) NULL,
  `city` VARCHAR(255) NULL,
  `state` VARCHAR(255) NULL,
  `zip` VARCHAR(255) NULL,
  `country` VARCHAR(255) NULL,
  `serialized_location` VARCHAR(255) NULL,
  `geo_json` LONGTEXT NULL,
  PRIMARY KEY (`location_id`),
  INDEX `ix_location_serialized_locati_46` (`serialized_location` ASC));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.log
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `time` DATETIME NULL,
  `user_id` LONGTEXT NULL,
  `path` LONGTEXT NULL,
  `action` LONGTEXT NULL,
  `resource_type` LONGTEXT NULL,
  `resource_uuid` LONGTEXT NULL,
  PRIMARY KEY (`id`))
AUTO_INCREMENT = 9000;

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.membership
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`membership` (
  `membership_type` VARCHAR(31) NOT NULL,
  `membership_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `expiration` BIGINT NULL,
  `status` VARCHAR(9) NULL,
  `creator_user_id` BIGINT NULL,
  `user_user_id` BIGINT NULL,
  `target_uuid` VARCHAR(40) NULL,
  `assembly_assembly_id` BIGINT NULL,
  `working_group_group_id` BIGINT NULL,
  PRIMARY KEY (`membership_id`),
  INDEX `ix_membership_assembly_28` (`assembly_assembly_id` ASC),
  INDEX `ix_membership_creator_26` (`creator_user_id` ASC),
  INDEX `ix_membership_user_27` (`user_user_id` ASC),
  INDEX `ix_membership_workinggroup_29` (`working_group_group_id` ASC),
  CONSTRAINT `fk_membership_assembly_28`
    FOREIGN KEY (`assembly_assembly_id`)
    REFERENCES `appcivistcore2`.`assembly` (`assembly_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_membership_creator_26`
    FOREIGN KEY (`creator_user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_membership_user_27`
    FOREIGN KEY (`user_user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_membership_workinggroup_29`
    FOREIGN KEY (`working_group_group_id`)
    REFERENCES `appcivistcore2`.`working_group` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.membership_invitation
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`membership_invitation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `email` VARCHAR(255) NULL,
  `user_id` BIGINT NULL,
  `status` VARCHAR(9) NULL,
  `creator_user_id` BIGINT NULL,
  `target_id` BIGINT NULL,
  `target_type` VARCHAR(8) NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_membership_invitation_crea_30` (`creator_user_id` ASC),
  CONSTRAINT `fk_membership_invitation_crea_30`
    FOREIGN KEY (`creator_user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.membership_invitation_security_r
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`membership_invitation_security_r` (
  `membership_invitation_id` BIGINT NOT NULL,
  `security_role_role_id` BIGINT NOT NULL,
  PRIMARY KEY (`membership_invitation_id`, `security_role_role_id`),
  CONSTRAINT `fk_membership_invitation_secu_01`
    FOREIGN KEY (`membership_invitation_id`)
    REFERENCES `appcivistcore2`.`membership_invitation` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_membership_invitation_secu_02`
    FOREIGN KEY (`security_role_role_id`)
    REFERENCES `appcivistcore2`.`security_role` (`role_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.membership_role
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`membership_role` (
  `membership_membership_id` BIGINT NOT NULL,
  `role_role_id` BIGINT NOT NULL,
  PRIMARY KEY (`membership_membership_id`, `role_role_id`),
  CONSTRAINT `fk_membership_role_membership_01`
    FOREIGN KEY (`membership_membership_id`)
    REFERENCES `appcivistcore2`.`membership` (`membership_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_membership_role_security_r_02`
    FOREIGN KEY (`role_role_id`)
    REFERENCES `appcivistcore2`.`security_role` (`role_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.properties
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`properties` (
  `properties_id` BIGINT NOT NULL AUTO_INCREMENT,
  `key` VARCHAR(255) NULL,
  `value` VARCHAR(255) NULL,
  `geo_location_id` BIGINT NULL,
  PRIMARY KEY (`properties_id`),
  INDEX `ix_properties_geo_31` (`geo_location_id` ASC),
  CONSTRAINT `fk_properties_geo_31`
    FOREIGN KEY (`geo_location_id`)
    REFERENCES `appcivistcore2`.`geo` (`location_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource` (
  `resource_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  `url` VARCHAR(255) NULL,
  `resource_type` VARCHAR(7) NULL,
  `name` VARCHAR(255) NULL,
  `pad_id` VARCHAR(255) NULL,
  `read_only_pad_id` VARCHAR(255) NULL,
  `resource_space_with_server_configs` VARCHAR(40) NULL,
  `url_large` VARCHAR(255) NULL,
  `url_medium` VARCHAR(255) NULL,
  `url_thumbnail` VARCHAR(255) NULL,
  PRIMARY KEY (`resource_id`),
  INDEX `ix_resource_uuid_47` (`uuid` ASC));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space` (
  `resource_space_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  `type` VARCHAR(13) NULL,
  `parent` VARCHAR(40) NULL,
  PRIMARY KEY (`resource_space_id`),
  INDEX `ix_resource_space_uuid_48` (`uuid` ASC));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_assemblies
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_assemblies` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `assembly_assembly_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `assembly_assembly_id`),
  CONSTRAINT `fk_resource_space_assemblies__01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_assemblies__02`
    FOREIGN KEY (`assembly_assembly_id`)
    REFERENCES `appcivistcore2`.`assembly` (`assembly_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_ballots
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_ballots` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `ballot_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `ballot_id`),
  CONSTRAINT `fk_resource_space_ballots_bal_02`
    FOREIGN KEY (`ballot_id`)
    REFERENCES `appcivistcore2`.`ballot` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_ballots_res_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_campaign
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_campaign` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `campaign_campaign_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `campaign_campaign_id`),
  CONSTRAINT `fk_resource_space_campaign_ca_02`
    FOREIGN KEY (`campaign_campaign_id`)
    REFERENCES `appcivistcore2`.`campaign` (`campaign_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_campaign_re_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_campaign_components
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_campaign_components` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `component_component_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `component_component_id`),
  CONSTRAINT `fk_resource_space_campaign_co_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_campaign_co_02`
    FOREIGN KEY (`component_component_id`)
    REFERENCES `appcivistcore2`.`component` (`component_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_campaign_milestones
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_campaign_milestones` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `component_milestone_component_milestone_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `component_milestone_component_milestone_id`),
  CONSTRAINT `fk_resource_space_campaign_mi_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_campaign_mi_02`
    FOREIGN KEY (`component_milestone_component_milestone_id`)
    REFERENCES `appcivistcore2`.`component_milestone` (`component_milestone_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_config
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_config` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `config_uuid` VARCHAR(40) NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `config_uuid`),
  CONSTRAINT `fk_resource_space_config_conf_02`
    FOREIGN KEY (`config_uuid`)
    REFERENCES `appcivistcore2`.`config` (`uuid`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_config_reso_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_contributions
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_contributions` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `contribution_contribution_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `contribution_contribution_id`),
  CONSTRAINT `fk_resource_space_contributio_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_contributio_02`
    FOREIGN KEY (`contribution_contribution_id`)
    REFERENCES `appcivistcore2`.`contribution` (`contribution_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_hashtag
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_hashtag` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `hashtag_hashtag_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `hashtag_hashtag_id`),
  CONSTRAINT `fk_resource_space_hashtag_has_02`
    FOREIGN KEY (`hashtag_hashtag_id`)
    REFERENCES `appcivistcore2`.`hashtag` (`hashtag_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_hashtag_res_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_resource
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_resource` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `resource_resource_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `resource_resource_id`),
  CONSTRAINT `fk_resource_space_resource_re_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_resource_re_02`
    FOREIGN KEY (`resource_resource_id`)
    REFERENCES `appcivistcore2`.`resource` (`resource_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_templates
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_templates` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `contribution_template_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `contribution_template_id`),
  CONSTRAINT `fk_resource_space_templates_c_02`
    FOREIGN KEY (`contribution_template_id`)
    REFERENCES `appcivistcore2`.`contribution_template` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_templates_r_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_theme
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_theme` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `theme_theme_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `theme_theme_id`),
  CONSTRAINT `fk_resource_space_theme_resou_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_theme_theme_02`
    FOREIGN KEY (`theme_theme_id`)
    REFERENCES `appcivistcore2`.`theme` (`theme_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.resource_space_working_groups
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`resource_space_working_groups` (
  `resource_space_resource_space_id` BIGINT NOT NULL,
  `working_group_group_id` BIGINT NOT NULL,
  PRIMARY KEY (`resource_space_resource_space_id`, `working_group_group_id`),
  CONSTRAINT `fk_resource_space_working_gro_01`
    FOREIGN KEY (`resource_space_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_resource_space_working_gro_02`
    FOREIGN KEY (`working_group_group_id`)
    REFERENCES `appcivistcore2`.`working_group` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.s3file
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`s3file` (
  `id` VARCHAR(40) NOT NULL,
  `bucket` VARCHAR(255) NULL,
  `name` VARCHAR(255) NULL,
  PRIMARY KEY (`id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.security_role
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`security_role` (
  `role_id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL,
  PRIMARY KEY (`role_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.theme
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`theme` (
  `theme_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `title` VARCHAR(255) NULL,
  `description` LONGTEXT NULL,
  `icon` VARCHAR(255) NULL,
  `cover` VARCHAR(255) NULL,
  PRIMARY KEY (`theme_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.token_action
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`token_action` (
  `token_id` BIGINT NOT NULL AUTO_INCREMENT,
  `token` VARCHAR(255) NULL,
  `user_id` BIGINT NULL,
  `membership_invitation_id` BIGINT NULL,
  `type` VARCHAR(2) NULL,
  `created` DATETIME NULL,
  `expires` DATETIME NULL,
  PRIMARY KEY (`token_id`),
  INDEX `ix_token_action_targetinvitat_33` (`membership_invitation_id` ASC),
  INDEX `ix_token_action_targetuser_32` (`user_id` ASC),
  UNIQUE INDEX `uq_token_action_membership_invit` (`membership_invitation_id` ASC),
  UNIQUE INDEX `uq_token_action_token` (`token` ASC),
  CONSTRAINT `fk_token_action_targetinvitat_33`
    FOREIGN KEY (`membership_invitation_id`)
    REFERENCES `appcivistcore2`.`membership_invitation` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_token_action_targetuser_32`
    FOREIGN KEY (`user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.user_permission
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`user_permission` (
  `permission_id` BIGINT NOT NULL AUTO_INCREMENT,
  `permission_value` VARCHAR(255) NULL,
  PRIMARY KEY (`permission_id`));

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.user_profile
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`user_profile` (
  `profile_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  `name` VARCHAR(255) NULL,
  `middle_name` VARCHAR(255) NULL,
  `last_name` VARCHAR(255) NULL,
  `birthdate` DATETIME NULL,
  `address` LONGTEXT NULL,
  `user_user_id` BIGINT NULL,
  PRIMARY KEY (`profile_id`),
  INDEX `ix_user_profile_user_35` (`user_user_id` ASC),
  UNIQUE INDEX `uq_user_profile_user_user_id` (`user_user_id` ASC),
  CONSTRAINT `fk_user_profile_user_35`
    FOREIGN KEY (`user_user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.user_security_roles
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`user_security_roles` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  CONSTRAINT `fk_user_security_roles_appciv_01`
    FOREIGN KEY (`user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_security_roles_securi_02`
    FOREIGN KEY (`role_id`)
    REFERENCES `appcivistcore2`.`security_role` (`role_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.user_user_permission
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`user_user_permission` (
  `user_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `permission_id`),
  CONSTRAINT `fk_user_user_permission_appci_01`
    FOREIGN KEY (`user_id`)
    REFERENCES `appcivistcore2`.`appcivist_user` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_user_permission_user__02`
    FOREIGN KEY (`permission_id`)
    REFERENCES `appcivistcore2`.`user_permission` (`permission_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.vote
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`vote` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `candidate_id` BIGINT NULL,
  `ballot_paper_id` BIGINT NULL,
  `value` LONGTEXT NULL,
  `value_type` INT NULL,
  `created_at` DATETIME NULL,
  `updated_at` DATETIME NULL,
  `removed_at` DATETIME NULL,
  `removed` TINYINT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_vote_ballot_paper`
    FOREIGN KEY (`ballot_paper_id`)
    REFERENCES `appcivistcore2`.`ballot_paper` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_vote_candidate`
    FOREIGN KEY (`candidate_id`)
    REFERENCES `appcivistcore2`.`candidate` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
AUTO_INCREMENT = 9000;

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.working_group
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`working_group` (
  `group_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `uuid` VARCHAR(40) NULL,
  `name` VARCHAR(255) NULL,
  `text` LONGTEXT NULL,
  `listed` TINYINT NULL,
  `majority_threshold` VARCHAR(255) NULL,
  `block_majority` TINYINT NULL,
  `profile_working_group_profile_id` BIGINT NULL,
  `invitationemail` LONGTEXT NULL,
  `resources_resource_space_id` BIGINT NULL,
  `forum_resource_space_id` BIGINT NULL,
  `consensus_ballot` VARCHAR(40) NULL,
  PRIMARY KEY (`group_id`),
  INDEX `ix_working_group_forum_38` (`forum_resource_space_id` ASC),
  INDEX `ix_working_group_profile_36` (`profile_working_group_profile_id` ASC),
  INDEX `ix_working_group_resources_37` (`resources_resource_space_id` ASC),
  UNIQUE INDEX `uq_working_group_forum_resource_` (`forum_resource_space_id` ASC),
  UNIQUE INDEX `uq_working_group_profile_working` (`profile_working_group_profile_id` ASC),
  UNIQUE INDEX `uq_working_group_resources_resou` (`resources_resource_space_id` ASC),
  CONSTRAINT `fk_working_group_forum_38`
    FOREIGN KEY (`forum_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_working_group_profile_36`
    FOREIGN KEY (`profile_working_group_profile_id`)
    REFERENCES `appcivistcore2`.`working_group_profile` (`working_group_profile_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_working_group_resources_37`
    FOREIGN KEY (`resources_resource_space_id`)
    REFERENCES `appcivistcore2`.`resource_space` (`resource_space_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table appcivistcore2.working_group_profile
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `appcivistcore2`.`working_group_profile` (
  `working_group_profile_id` BIGINT NOT NULL AUTO_INCREMENT,
  `creation` DATETIME NULL,
  `last_update` DATETIME NULL,
  `lang` VARCHAR(255) NULL,
  `removal` DATETIME NULL,
  `removed` TINYINT NULL,
  `supported_membership` VARCHAR(22) NULL,
  `management_type` VARCHAR(25) NULL,
  `icon` VARCHAR(255) NULL,
  `cover` VARCHAR(255) NULL,
  PRIMARY KEY (`working_group_profile_id`));
SET FOREIGN_KEY_CHECKS = 1;
