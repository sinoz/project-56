-- @Author: Maurice van Veen
-- @Author: Ilyas Baas

# --- !Ups

DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS gameaccounts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS gamecategories;
DROP TABLE IF EXISTS couponcodes;

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
  password VARCHAR(256),
  passwordsalt VARCHAR(256),
  mail VARCHAR(128),
  profilepicture VARCHAR(256),  -- url to profile picture
  paymentmail VARCHAR(128),
  inventory INTEGER[],     -- list of product ids
  favorites INTEGER[],     -- list of product ids
  orderhistory INTEGER[],  -- list of order ids
  membersince TIMESTAMP,
  sessionToken TEXT,       -- token for session
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
  trackid VARCHAR(36),
  hasuser BOOLEAN,
  userid INTEGER, -- buying user id
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

CREATE TABLE couponcodes (
  id SERIAL PRIMARY KEY,
  code VARCHAR(128),
  percentage DOUBLE PRECISION,
  end_date TIMESTAMP
);

INSERT INTO couponcodes(code, percentage) VALUES ('dank', 19);
INSERT INTO couponcodes(code, percentage) VALUES ('johan', -1);
INSERT INTO couponcodes(code, percentage) VALUES ('joris', 98);
INSERT INTO couponcodes(code, percentage) VALUES ('maurice', 99);
INSERT INTO couponcodes(code, percentage) VALUES ('ilyas', 100);

INSERT INTO users (username, password, passwordsalt, mail, profilepicture, paymentmail, inventory, favorites, orderhistory, membersince, isadmin) VALUES
  (
    'admin', -- username
    'u7+UejmS48mFKzGIiB1+U10r7u5GB6K7zwF2ryZf9e0lFU/7Ww/7YkAB9aa0PzWkfGtBrPcxPrXQYf6Ycz7kX8ZPB0ye7MQSRQNWBq17kro=', -- password (admin)
    '032aee72a91f745f6a94bbc7fda17f09c5b65d0bd2046c3bba62724e15439ea2', -- password salt
    'contact@ReStart.com', -- mail
    'images/dankadmin.gif', -- profile picture
    'payment@ReStart.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    TRUE -- is admin
  ),
  (
    'default', -- username
    'HdbKZo6GXoopL8H3ug+CjQQTylK9yDA6uiX/bikFGQ12mLvwEDIkqqmYq1GNMLlu/pHq5IPC8KXjFLiqNCGdhsZPB0ye7MQSRQNWBq17kro=', -- password (default)
    '5f0571f13aa8d39be462ecabd825090e7aeaf19e18cd464221866b8532a37271', -- password salt
    'test@example.com', -- mail
    'images/default_profile_pic.png', -- profile picture
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
  -- This gameaccount has been bought by admin and sold by default
  (
    2, -- userid
    1, -- gameid
    FALSE , -- visible
    TRUE, -- disabled
    'CSGO: Pro Player', -- title,
    'This game has been bought by the admin', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    105.00, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'password' -- passwordcurrent
  ),
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
    1, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'CSGO: Medium Player', -- title,
    'This is a Medium Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    25.25, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    2, -- userid
    1, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'CSGO: Worst Player', -- title,
    'This is a Worst Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    -10.00, -- buyprice
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

INSERT INTO orders (trackid, hasuser, userid, productid, price, couponcode, status) VALUES
  (
    '54947df8-0e9e-4471-a2f9-9af509fb5889', -- trackid
    TRUE, -- hasuser
    1, -- userid
    1, -- productid
    105.00, -- price
    '', -- couponcode
    0 -- status
  );

INSERT INTO reviews (userreceiverid, usersenderid, title, description, rating) VALUES
  (
    '1', -- userreceiverid
    '2', -- usersenderid
    'Great Seller', -- Title
    'Hello am 48 year man from somalia. Sorry for my bed england. I selled my wife for internet connection for play "conter strik" and i want to become the goodest player like you I play with 400 ping on brazil and i am global elite 2. pls no copy pasterio my story ', -- desription
    '5' --  rating
  ),

  (
    '1', -- userreceiverid
    '2', -- usersenderid
    'got extra dank memes', -- Title
    'Hello am 48 year man from somalia. Sorry for my bed england. I selled my wife for internet connection for play "conter strik" and i want to become the goodest player like you I play with 400 ping on brazil and i am global elite 2. pls no copy pasterio my story ', -- desription
    '4' --  rating
  ),

  (
    '1', -- userreceiverid
    '2', -- usersenderid
    'dankest Seller', -- Title
    'Hello am 48 year man from somalia. Sorry for my bed england. I selled my wife for internet connection for play "conter strik" and i want to become the goodest player like you I play with 400 ping on brazil and i am global elite 2. pls no copy pasterio my story ', -- desription
    '3' --  rating
  );

--- !Downs