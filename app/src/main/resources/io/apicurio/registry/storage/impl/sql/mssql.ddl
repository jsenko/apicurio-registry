-- *********************************************************************
-- DDL for the Apicurio Registry - Database: MS SQL Server
-- *********************************************************************

CREATE TABLE apicurio (propName NVARCHAR(255) NOT NULL, propValue NVARCHAR(255));
ALTER TABLE apicurio ADD PRIMARY KEY (propName);
INSERT INTO apicurio (propName, propValue) VALUES ('db_version', 101);

CREATE TABLE sequences (seqName NVARCHAR(32) NOT NULL, seqValue BIGINT NOT NULL);
ALTER TABLE sequences ADD PRIMARY KEY (seqName);

CREATE TABLE config (propName NVARCHAR(255) NOT NULL, propValue NVARCHAR(1024) NOT NULL, modifiedOn BIGINT NOT NULL);
ALTER TABLE config ADD PRIMARY KEY (propName);
CREATE INDEX IDX_config_1 ON config(modifiedOn);

CREATE TABLE acls (principalId NVARCHAR(256) NOT NULL, role NVARCHAR(32) NOT NULL, principalName NVARCHAR(256));
ALTER TABLE acls ADD PRIMARY KEY (principalId);

CREATE TABLE downloads (downloadId NVARCHAR(128) NOT NULL, expires BIGINT NOT NULL, context NVARCHAR(1024));
ALTER TABLE downloads ADD PRIMARY KEY (downloadId);
CREATE INDEX IDX_down_1 ON downloads(expires);

CREATE TABLE global_rules (type NVARCHAR(32) NOT NULL, configuration TEXT NOT NULL);
ALTER TABLE global_rules ADD PRIMARY KEY (type);

CREATE TABLE content (contentId BIGINT NOT NULL, canonicalHash NVARCHAR(64) NOT NULL, contentHash NVARCHAR(64) NOT NULL, contentType NVARCHAR(64) NOT NULL, content VARBINARY(MAX) NOT NULL, refs TEXT);
ALTER TABLE content ADD PRIMARY KEY (contentId);
ALTER TABLE content ADD CONSTRAINT UQ_content_1 UNIQUE (contentHash);
CREATE INDEX IDX_content_1 ON content(canonicalHash);
CREATE INDEX IDX_content_2 ON content(contentHash);

CREATE TABLE content_references (contentId BIGINT NOT NULL, groupId NVARCHAR(512), artifactId NVARCHAR(512) NOT NULL, version NVARCHAR(256), name NVARCHAR(512) NOT NULL);
ALTER TABLE content_references ADD PRIMARY KEY (contentId, name);
ALTER TABLE content_references ADD CONSTRAINT FK_content_references_1 FOREIGN KEY (contentId) REFERENCES content(contentId) ON DELETE CASCADE;

CREATE TABLE groups (groupId NVARCHAR(512) NOT NULL, description NVARCHAR(1024), artifactsType NVARCHAR(32), owner NVARCHAR(256), createdOn DATETIME2(6) NOT NULL, modifiedBy NVARCHAR(256), modifiedOn DATETIME2(6), labels TEXT);
ALTER TABLE groups ADD PRIMARY KEY (groupId);

CREATE TABLE group_labels (groupId NVARCHAR(512) NOT NULL, labelKey NVARCHAR(256) NOT NULL, labelValue NVARCHAR(512));
ALTER TABLE group_labels ADD CONSTRAINT FK_glabels_1 FOREIGN KEY (groupId) REFERENCES groups(groupId) ON DELETE CASCADE;
CREATE INDEX IDX_glabels_1 ON group_labels(labelKey);
CREATE INDEX IDX_glabels_2 ON group_labels(labelValue);

CREATE TABLE group_rules (groupId NVARCHAR(512) NOT NULL, type NVARCHAR(32) NOT NULL, configuration NVARCHAR(1024) NOT NULL);
ALTER TABLE group_rules ADD PRIMARY KEY (groupId, type);
ALTER TABLE group_rules ADD CONSTRAINT FK_grules_1 FOREIGN KEY (groupId) REFERENCES groups(groupId) ON DELETE CASCADE;

CREATE TABLE artifacts (groupId NVARCHAR(512) NOT NULL, artifactId NVARCHAR(512) NOT NULL, type NVARCHAR(32) NOT NULL, owner NVARCHAR(256), createdOn DATETIME2(6) NOT NULL, modifiedBy NVARCHAR(256), modifiedOn DATETIME2(6), name NVARCHAR(512), description NVARCHAR(1024), labels TEXT);
ALTER TABLE artifacts ADD PRIMARY KEY (groupId, artifactId);
CREATE INDEX IDX_artifacts_0 ON artifacts(type);
CREATE INDEX IDX_artifacts_1 ON artifacts(owner);
CREATE INDEX IDX_artifacts_2 ON artifacts(createdOn);
CREATE INDEX IDX_artifacts_3 ON artifacts(name);
-- CREATE INDEX IDX_artifacts_4 ON artifacts(description);

CREATE TABLE artifact_labels (groupId NVARCHAR(512) NOT NULL, artifactId NVARCHAR(512) NOT NULL, labelKey NVARCHAR(256) NOT NULL, labelValue NVARCHAR(512));
ALTER TABLE artifact_labels ADD CONSTRAINT FK_alabels_1 FOREIGN KEY (groupId, artifactId) REFERENCES artifacts(groupId, artifactId) ON DELETE CASCADE;
CREATE INDEX IDX_alabels_1 ON artifact_labels(labelKey);
CREATE INDEX IDX_alabels_2 ON artifact_labels(labelValue);

CREATE TABLE artifact_rules (groupId NVARCHAR(512) NOT NULL, artifactId NVARCHAR(512) NOT NULL, type NVARCHAR(32) NOT NULL, configuration NVARCHAR(1024) NOT NULL);
ALTER TABLE artifact_rules ADD PRIMARY KEY (groupId, artifactId, type);
-- Note: no FK constraint between artifact_rules and artifacts because the Confluent API allows
--       rules to be configured for Artifacts that do not yet exist.

-- The "versionOrder" field is needed to generate "version" when it is not provided.
-- It contains the same information as the "branchOrder" in the "latest" branch, but we cannot use it because of a chicken-and-egg problem.
-- At least it is no longer confusingly called "versionId". The "versionOrder" field should not be used for any other purpose.
CREATE TABLE versions (globalId BIGINT NOT NULL, groupId NVARCHAR(512) NOT NULL, artifactId NVARCHAR(512) NOT NULL, version NVARCHAR(256), versionOrder INT NOT NULL, state NVARCHAR(64) NOT NULL, name NVARCHAR(512), description NVARCHAR(1024), owner NVARCHAR(256), createdOn DATETIME2(6) NOT NULL, modifiedBy NVARCHAR(256), modifiedOn DATETIME2(6) NOT NULL, labels TEXT, contentId BIGINT NOT NULL);
ALTER TABLE versions ADD PRIMARY KEY (globalId);
ALTER TABLE versions ADD CONSTRAINT UQ_versions_1 UNIQUE (groupId, artifactId, version);
ALTER TABLE versions ADD CONSTRAINT UQ_versions_2 UNIQUE (globalId, versionOrder);
ALTER TABLE versions ADD CONSTRAINT FK_versions_1 FOREIGN KEY (groupId, artifactId) REFERENCES artifacts(groupId, artifactId) ON DELETE CASCADE;
ALTER TABLE versions ADD CONSTRAINT FK_versions_2 FOREIGN KEY (contentId) REFERENCES content(contentId);
CREATE INDEX IDX_versions_1 ON versions(version);
CREATE INDEX IDX_versions_2 ON versions(state);
CREATE INDEX IDX_versions_3 ON versions(name);
-- CREATE INDEX IDX_versions_4 ON versions(description); TODO: Why commented out? Maybe the same as IDX_props_2?
CREATE INDEX IDX_versions_5 ON versions(owner);
CREATE INDEX IDX_versions_6 ON versions(createdOn);
CREATE INDEX IDX_versions_7 ON versions(contentId);

CREATE TABLE version_labels (globalId BIGINT NOT NULL, labelKey NVARCHAR(256) NOT NULL, labelValue NVARCHAR(512));
ALTER TABLE version_labels ADD CONSTRAINT FK_vlabels_1 FOREIGN KEY (globalId) REFERENCES versions(globalId) ON DELETE CASCADE;
CREATE INDEX IDX_vlabels_1 ON version_labels(labelKey);
CREATE INDEX IDX_vlabels_2 ON version_labels(labelValue);

CREATE TABLE version_comments (commentId NVARCHAR(128) NOT NULL, globalId BIGINT NOT NULL, owner NVARCHAR(256), createdOn DATETIME2(6) NOT NULL, cvalue NVARCHAR(1024) NOT NULL);
ALTER TABLE version_comments ADD PRIMARY KEY (commentId);
ALTER TABLE version_comments ADD CONSTRAINT FK_version_comments_1 FOREIGN KEY (globalId) REFERENCES versions(globalId) ON DELETE CASCADE;
CREATE INDEX IDX_version_comments_1 ON version_comments(owner);

-- This table is defined way down here because it has a FK to the artifacts table *and* the versions table
CREATE TABLE branches (groupId NVARCHAR(512) NOT NULL, artifactId NVARCHAR(512) NOT NULL, branchId NVARCHAR(256) NOT NULL, description NVARCHAR(1024), systemDefined BIT NOT NULL, owner NVARCHAR(256), createdOn DATETIME2(6) NOT NULL, modifiedBy NVARCHAR(256), modifiedOn DATETIME2(6) NOT NULL);
ALTER TABLE branches ADD PRIMARY KEY (groupId, artifactId, branchId);
ALTER TABLE branches ADD CONSTRAINT FK_branches_1 FOREIGN KEY (groupId, artifactId) REFERENCES artifacts(groupId, artifactId) ON DELETE CASCADE;

CREATE TABLE branch_versions (groupId NVARCHAR(512) NOT NULL, artifactId NVARCHAR(512) NOT NULL, branchId NVARCHAR(256) NOT NULL, branchOrder INT NOT NULL, version NVARCHAR(256) NOT NULL);
ALTER TABLE branch_versions ADD PRIMARY KEY (groupId, artifactId, branchId, version);
ALTER TABLE branch_versions ADD CONSTRAINT FK_branch_versions_1 FOREIGN KEY (groupId, artifactId, branchId) REFERENCES branches(groupId, artifactId, branchId);
ALTER TABLE branch_versions ADD CONSTRAINT FK_branch_versions_2 FOREIGN KEY (groupId, artifactId, version) REFERENCES versions(groupId, artifactId, version) ON DELETE CASCADE;
CREATE INDEX IDX_branch_versions_1 ON branch_versions(groupId, artifactId, branchId, branchOrder);
CREATE INDEX IDX_branch_versions_2 ON branch_versions(branchId);
CREATE INDEX IDX_branch_versions_3 ON branch_versions(branchOrder);

CREATE TABLE outbox (id VARCHAR(128) NOT NULL, aggregatetype VARCHAR(255) NOT NULL, aggregateid VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL, payload TEXT NOT NULL);
ALTER TABLE outbox ADD PRIMARY KEY (id);
