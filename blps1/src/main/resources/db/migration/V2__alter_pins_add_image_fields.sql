alter table pins
    add column image_url varchar(1000);

alter table pins
    add column image_key varchar(500);

update pins
set image_url = content_url,
    image_key = 'legacy/' || id
where image_url is null;

alter table pins
    alter column image_url set not null;

alter table pins
    alter column image_key set not null;

alter table pins
drop column content_url;