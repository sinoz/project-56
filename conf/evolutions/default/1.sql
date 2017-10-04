-- @Author: Maurice van Veen

# --- !Ups

DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS gameaccounts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS gamecategories;

CREATE TABLE gamecategories (
  id SERIAL PRIMARY KEY,
  name VARCHAR(32),
  image VARCHAR(256), -- url to profile picture
  description TEXT
  --   TODO: optionally add specifications
);

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(16),
  password VARCHAR(64),
  passwordsalt VARCHAR(256),
  mail VARCHAR(128),
  profilepicture VARCHAR(256),  -- url to profile picture
  paymentmail VARCHAR(128),
  inventory INTEGER[],     -- list of product ids
  favorites INTEGER[],     -- list of product ids
  orderhistory INTEGER[],  -- list of order ids
  membersince TIMESTAMP,
  isadmin BOOLEAN
);

CREATE TABLE gameaccounts (
  id SERIAL PRIMARY KEY,
  userid INTEGER REFERENCES users(id), -- selling user id
  gameid INTEGER REFERENCES gamecategories(id), -- game associated
  visible BOOLEAN, -- visible in web shop / available for purchase
  disabled BOOLEAN, -- not reachable, only visible in order history
  title VARCHAR(32),
  description TEXT,
  addedsince TIMESTAMP,
  canbuy BOOLEAN,
  buyprice DOUBLE PRECISION,
  cantrade BOOLEAN,
  maillast VARCHAR(128), -- last know mail address used for the account
  mailcurrent VARCHAR(128), -- current mail address used for login
  passwordcurrent VARCHAR(64) -- current password used for login
);

CREATE TABLE orders (
  id SERIAL PRIMARY KEY,
  userid INTEGER REFERENCES users(id), -- buying user id
  productid INTEGER REFERENCES gameaccounts(id), -- associated bought game account
  price DOUBLE PRECISION,
  couponcode VARCHAR(128),
  status INT -- TODO: change to enum, status of the order
);

CREATE TABLE reviews (
  id SERIAL PRIMARY KEY,
  userreceiverid INTEGER REFERENCES users(id),
  usersenderid INTEGER REFERENCES users(id),
  title VARCHAR(64),
  description TEXT,
  rating INTEGER
);

INSERT INTO users (username, password, passwordsalt, mail, profilepicture, paymentmail, inventory, favorites, orderhistory, membersince, isadmin) VALUES
  (
    'admin', -- username
    'admin', -- password
    'salt', -- password salt
    'contact@ReStart.com', -- mail
    'http://www.yurisrunhouston.com/uploads/3/9/1/9/3919285/_457014621.png', -- profile picture
    'payment@ReStart.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    TRUE -- is admin
  ),
  (
    'default', -- username
    'default', -- password
    'salt', -- password salt
    'test@example.com', -- mail
    'http://www.zsbd.org.bd/assets/images/default1.gif', -- profile picture
    'payment@example.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    FALSE -- is admin
  );

INSERT INTO gamecategories (name, image, description) VALUES
  (
    'Counter-Strike: Global Offensive', -- name
    'images/CSGO-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'Overwatch', -- name
    'images/Overwatch-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'World of Warcraft', -- name
    'images/WoW-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'League of Legends', -- name
    'images/LoL-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'Minecraft', -- name
    'images/Minecraft-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    E'PLAYERUNKNOWN\'S BATTLEGROUNDS', -- name
    'images/PUBG.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'Destiny 2', -- name
    'images/Destiny-2-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'Hearthstone', -- name
    'images/Hearthstone-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'Dota 2', -- name
    'images/Dota_2-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'Rocket League', -- name
    'images/Rocket_League-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'For Honor', -- name
    'images/For_Honor-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'Call of Duty: WWII', -- name
    'images/CoD_WW2-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'Starcraft II', -- name
    'images/Starcraft_2-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'RuneScape', -- name
    'images/RuneScape-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'Heroes of the Storm', -- name
    'images/Heroes_of_the_Storm-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'Grand Theft Auto V', -- name
    'images/GTA5-Cover.png', -- url to image
    'TODO description' -- description
  );

INSERT INTO gameaccounts (userid, gameid, visible, disabled, title, description, addedsince, canbuy, buyprice, cantrade, maillast, mailcurrent, passwordcurrent) VALUES
  (
    1, -- userid
    1, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'CSGO: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    2, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Overwatch: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    3, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'WoW: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    4, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'LoL: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    5, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Minecraft: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    6, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'PUBG: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    7, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'D2: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    8, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'HS: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    9, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Dota2: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    10, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'RL: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    11, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'FH: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    12, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'CoDWWII: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    13, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'SCII: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    15, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'HofS: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    1, -- userid
    16, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'GTAV: Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    52.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  );

--- !Downs