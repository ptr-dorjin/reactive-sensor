create table if not exists sensor_data
(
    id          varchar(60) default random_uuid() primary key,
    temperature double       not null,
    location    varchar(128) not null,
    instant     timestamp    not null
);