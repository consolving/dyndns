# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  id                        bigint auto_increment not null,
  username                  varchar(255),
  constraint pk_account primary key (id))
;

create table dns_entry (
  id                        bigint auto_increment not null,
  created                   timestamp,
  updated                   timestamp,
  changed                   timestamp,
  updated_ip                varchar(255),
  actual_ip                 varchar(255),
  name                      varchar(255),
  api_key                   varchar(255),
  to_delete                 boolean,
  account_id                bigint,
  domain_id                 bigint,
  sub_domain_id             bigint,
  constraint pk_dns_entry primary key (id))
;

create table domain (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  hostmaster                varchar(255),
  ip                        varchar(255),
  code                      varchar(255),
  force_update              boolean,
  constraint pk_domain primary key (id))
;

create table sub_domain (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  domain_id                 bigint,
  constraint pk_sub_domain primary key (id))
;

alter table dns_entry add constraint fk_dns_entry_account_1 foreign key (account_id) references account (id) on delete restrict on update restrict;
create index ix_dns_entry_account_1 on dns_entry (account_id);
alter table dns_entry add constraint fk_dns_entry_domain_2 foreign key (domain_id) references domain (id) on delete restrict on update restrict;
create index ix_dns_entry_domain_2 on dns_entry (domain_id);
alter table dns_entry add constraint fk_dns_entry_subDomain_3 foreign key (sub_domain_id) references sub_domain (id) on delete restrict on update restrict;
create index ix_dns_entry_subDomain_3 on dns_entry (sub_domain_id);
alter table sub_domain add constraint fk_sub_domain_domain_4 foreign key (domain_id) references domain (id) on delete restrict on update restrict;
create index ix_sub_domain_domain_4 on sub_domain (domain_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists account;

drop table if exists dns_entry;

drop table if exists domain;

drop table if exists sub_domain;

SET REFERENTIAL_INTEGRITY TRUE;

