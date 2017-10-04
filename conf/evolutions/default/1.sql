-- @Author: Maurice van Veen

# --- !Ups

DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS gameaccounts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS gamecategories;

CREATE TABLE gamecategories (
  id TEXT PRIMARY KEY,
  name TEXT,
  image TEXT, -- url to profile picture
  description TEXT
  --   TODO: optionally add specifications
);

CREATE TABLE users (
  id TEXT PRIMARY KEY,
  username TEXT,
  password TEXT,
  passwordsalt TEXT,
  mail TEXT,
  profilepicture TEXT,  -- url to profile picture
  paymentmail TEXT,
  inventory TEXT[],     -- list of product ids
  favorites TEXT[],     -- list of product ids
  orderhistory TEXT[],  -- list of order ids
  membersince TIMESTAMP,
  isadmin BOOLEAN
);

CREATE TABLE gameaccounts (
  id TEXT PRIMARY KEY,
  userid TEXT REFERENCES users(id), -- selling user id
  gameid TEXT REFERENCES gamecategories(id), -- game associated
  visible BOOLEAN, -- visible in web shop / available for purchase
  disabled BOOLEAN, -- not reachable, only visible in order history
  title TEXT,
  description TEXT,
  addedsince TIMESTAMP,
  canbuy BOOLEAN,
  buyprice DOUBLE PRECISION,
  cantrade BOOLEAN,
  maillast TEXT, -- last know mail address used for the account
  mailcurrent TEXT, -- current mail address used for login
  passwordcurrent TEXT -- current password used for login
);

CREATE TABLE orders (
  id TEXT PRIMARY KEY,
  userid TEXT REFERENCES users(id), -- buying user id
  productid TEXT REFERENCES gameaccounts(id), -- associated bought game account
  price DOUBLE PRECISION,
  couponcode TEXT,
  status INT -- TODO: change to enum, status of the order
);

CREATE TABLE reviews (
  id TEXT PRIMARY KEY,
  userreceiverid TEXT REFERENCES users(id),
  usersenderid TEXT REFERENCES users(id),
  title TEXT,
  description TEXT,
  rating INT
);

INSERT INTO users VALUES
  (
    'admin1', -- id
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
    'default1', -- id
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

INSERT INTO gamecategories VALUES
  (
    'G0001', -- id
    'Counter-Strike: Global Offensive', -- name
    'images/CSGO-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'G0002', -- id
    'Overwatch', -- name
    'images/Overwatch-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'G0003', -- id
    'World of Warcraft', -- name
    'images/WoW-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'G0004', -- id
    'League of Legends', -- name
    'images/LoL-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'G0005', -- id
    'Minecraft', -- name
    'images/Minecraft-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'G0006', -- id
    E'PLAYERUNKNOWN\'S BATTLEGROUNDS', -- name
    'images/PUBG.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'G0007', -- id
    'Destiny 2', -- name
    'images/Destiny-2-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'G0008', -- id
    'Hearthstone', -- name
    'images/Hearthstone-Cover.jpg', -- url to image
    'TODO description' -- description
  ),
  (
    'G0009', -- id
    'Dota 2', -- name
    'images/Dota_2-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'G0010', -- id
    'Rocket League', -- name
    'images/Rocket_League-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'G0011', -- id
    'For Honor', -- name
    'images/For_Honor-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'G0012', -- id
    'Call of Duty: WWII', -- name
    'images/CoD_WW2-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'G0013', -- id
    'Starcraft II', -- name
    'images/Starcraft_2-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'G0014', -- id
    'RuneScape', -- name
    'images/RuneScape-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'G0015', -- id
    'Heroes of the Storm', -- name
    'images/Heroes_of_the_Storm-Cover.png', -- url to image
    'TODO description' -- description
  ),
  (
    'G0016', -- id
    'Grand Theft Auto V', -- name
    'images/GTA5-Cover.png', -- url to image
    'TODO description' -- description
  );

INSERT INTO gameaccounts VALUES
  (
    'GA000001', -- id
                'admin1', -- userid
                'G0001', -- gameid
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
    'GA000002', -- id
                'admin1', -- userid
                'G0002', -- gameid
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
    'GA000003', -- id
                'admin1', -- userid
                'G0003', -- gameid
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
    'GA000004', -- id
                'admin1', -- userid
                'G0004', -- gameid
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
    'GA000005', -- id
                'admin1', -- userid
                'G0005', -- gameid
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
    'GA000006', -- id
                'admin1', -- userid
                'G0006', -- gameid
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
    'GA000007', -- id
                'admin1', -- userid
                'G0007', -- gameid
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
    'GA000008', -- id
                'admin1', -- userid
                'G0008', -- gameid
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
    'GA000009', -- id
                'admin1', -- userid
                'G0009', -- gameid
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
    'GA000010', -- id
                'admin1', -- userid
                'G0010', -- gameid
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
    'GA000011', -- id
                'admin1', -- userid
                'G0011', -- gameid
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
    'GA000012', -- id
                'admin1', -- userid
                'G0012', -- gameid
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
    'GA000013', -- id
                'admin1', -- userid
                'G0013', -- gameid
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
    'GA000015', -- id
                'admin1', -- userid
                'G0015', -- gameid
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
    'GA000016', -- id
                'admin1', -- userid
                'G0016', -- gameid
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