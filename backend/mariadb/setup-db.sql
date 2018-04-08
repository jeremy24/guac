

CREATE TABLE users
(
  id int PRIMARY KEY AUTO_INCREMENT,
  username varchar(255),
  password varchar(255),
  public_key varchar(255)
);



drop procedure IF EXISTS add_user
create procedure add_user(
  IN username varchar(255),
  IN pass varchar(255),
  public_key varchar(255)
)
BEGIN

  # insert only if not exists
  IF NOT EXISTS(SELECT 1 FROM users AS a WHERE a.username = username) THEN
    INSERT INTO users(username, password, public_key)
    VALUES(username, pass, NULL);
  END IF;

  SELECT
    *
  FROM
    users AS a
  WHERE
    a.username = username;
END;


create procedure get_user (
  IN username varchar(255)
)
BEGIN

  SELECT
    *
  FROM
    users AS a
  WHERE
    a.username = username;
end;


drop procedure if exists update_key

create procedure update_key(
  IN username varchar(255),
  IN public_key varchar(255)
)
BEGIN

  UPDATE
    users AS a
  SET
    a.public_key = public_key
  WHERE
    a.username = username;

  SELECT
    *
  FROM
    users AS a
  WHERE
    a.username = username;
end;



call add_user('bob', 'password', NULL)
call update_key('bob', 'another key')
call get_user('bob')




