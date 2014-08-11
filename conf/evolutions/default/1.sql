# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  id                        bigint not null,
  username                  varchar(255),
  constraint pk_account primary key (id))
;

create table dns_entry (
  id                        bigint not null,
  created                   timestamp,
  updated                   timestamp,
  changed                   timestamp,
  updated_ip                varchar(255),
  actual_ip                 varchar(255),
  name                      varchar(255),
  api_key                   varchar(255),
  account_id                bigint,
  domain_id                 bigint,
  constraint pk_dns_entry primary key (id))
;

create table domain (
  id                        bigint not null,
  name                      varchar(255),
  hostmaster                varchar(255),
  ip                        varchar(255),
  code                      varchar(255),
  constraint pk_domain primary key (id))
;

create sequence account_seq;

create sequence dns_entry_seq;

create sequence domain_seq;

alter table dns_entry add constraint fk_dns_entry_account_1 foreign key (account_id) references account (id) on delete restrict on update restrict;
create index ix_dns_entry_account_1 on dns_entry (account_id);
alter table dns_entry add constraint fk_dns_entry_domain_2 foreign key (domain_id) references domain (id) on delete restrict on update restrict;
create index ix_dns_entry_domain_2 on dns_entry (domain_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists account;

drop table if exists dns_entry;

drop table if exists domain;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists account_seq;

drop sequence if exists dns_entry_seq;

drop sequence if exists domain_seq;

