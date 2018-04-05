create table ACTIVITIES(AId int, AName varchar(32));
create table SPORTS(AId int, AName varchar(32));
insert into ACTIVITIES(AId, AName) values (1, 'ACM');
insert into ACTIVITIES(AId, AName) values (2, 'GDC');
insert into ACTIVITIES(AId, AName) values (3, 'CSC');
insert into ACTIVITIES(AId, AName) values (4, 'comedy club');
insert into ACTIVITIES(AId, AName) values (5, 'art');
insert into ACTIVITIES(AId, AName) values (6, 'WiCS');
insert into SPORTS(AId, AName) values (1, 'volleyball');
insert into SPORTS(AId, AName) values (2, 'soccer');
insert into SPORTS(AId, AName) values (3, 'ski team');
insert into SPORTS(AId, AName) values (4, 'swimming');
insert into SPORTS(AId, AName) values (5, 'basketball');
insert into SPORTS(AId, AName) values (6, 'badminton');
select AId from SPORTS where AName = 'soccer';
-- result: 2
select AId from ACTIVITIES where AName = 'GDC';
-- result: 2
select AId from ACTIVITIES where AName = 'CSC';
-- result: 3
