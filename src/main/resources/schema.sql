create table if not exists sensor_data
(
    id          varchar(60) default random_uuid() primary key,
    temperature int          not null,
    location    varchar(128) not null,
    instant     timestamp    not null
);