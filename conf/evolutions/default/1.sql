-- @Author: Maurice van Veen
-- @Author: Ilyas Baas

# --- !Ups

DROP TABLE IF EXISTS visittimes;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS reviewtokens;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS gameaccounts;
DROP TABLE IF EXISTS users_change_password;
DROP TABLE IF EXISTS users_verification;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS gamecategories;
DROP TABLE IF EXISTS couponcodes;

CREATE TABLE visittimes (
  id SERIAL PRIMARY KEY,
  userid INT,
  time TIMESTAMP
);

CREATE TABLE gamecategories (
  id SERIAL PRIMARY KEY,
  name VARCHAR(32),
  image VARCHAR(256), -- url to profile picture
  description TEXT,
  search INT,
  genre VARCHAR(16)
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

CREATE TABLE users_verification (
  id SERIAL PRIMARY KEY,
  verification TEXT,
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

CREATE TABLE users_change_password (
  id SERIAL PRIMARY KEY,
  verification TEXT,
  username VARCHAR(16),
  mail VARCHAR(128),
  userid INTEGER REFERENCES users(id)
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
  productid INTEGER, -- associated bought/added game account
  price DOUBLE PRECISION,
  couponcode VARCHAR(128),
  ordertype INT, -- type of order
  status INT, -- status of order
  orderplaced TIMESTAMP
);

CREATE TABLE reviews (
  id SERIAL PRIMARY KEY,
  userreceiverid INTEGER REFERENCES users(id),
  usersenderid INTEGER REFERENCES users(id),
  title VARCHAR(64),
  description TEXT,
  rating INTEGER
);

CREATE TABLE reviewtokens (
  id SERIAL PRIMARY KEY,
  reviewid VARCHAR(36),
  userreceiverid INTEGER REFERENCES users(id),
  usersenderid INTEGER REFERENCES users(id),
  productid INTEGER REFERENCES gameaccounts(id)
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
INSERT INTO couponcodes(code, percentage) VALUES ('ilyas', 99);

INSERT INTO users (username, password, passwordsalt, mail, profilepicture, paymentmail, inventory, favorites, orderhistory, membersince, isadmin) VALUES
  (
    'admin', -- username
    'u7+UejmS48mFKzGIiB1+U10r7u5GB6K7zwF2ryZf9e0lFU/7Ww/7YkAB9aa0PzWkfGtBrPcxPrXQYf6Ycz7kX8ZPB0ye7MQSRQNWBq17kro=', -- password (admin)
    '032aee72a91f745f6a94bbc7fda17f09c5b65d0bd2046c3bba62724e15439ea2', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/dankadmin.gif', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
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
    'restartcontactus@gmail.com', -- mail
    'images/default_profile_pic.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    FALSE -- is admin
  ),
  (
    'newuser', -- username
    'CSmVWiwzNq2UnFlXBhe0v1XTFHRddYZu6f6Sm9ZX6yCufNmGRfJG2gWeICdtt0kuaSUwd8h3zxqdUoya3TF4fsZPB0ye7MQSRQNWBq17kro=', -- password (newuser)
    'd515b3fa1c9193ac3029fc7a5ebe51080f77f409f6ae76bc9482f5016c9708e2', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/default_profile_pic.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    FALSE -- is admin
  ),
  (
    'minecrafter', -- username
    'A0S2hfy6/m8gPO/KiamVgdsgOtnMs7MO2efPdIJeqOEfAz1nyx2xVuN6qk4zX3O3pMUsHoglPAffgbkv8BOKEcZPB0ye7MQSRQNWBq17kro=', -- password (minecrafter)
    '40fe473973db59dd8a613c7f86db99c1949cba9971d7f2a9bb422fa9f9ff2091', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/minecrafter.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    FALSE -- is admin
  ),
  (
    'runescaper', -- username
    'DgCc7ALRvGhyowScpMXu2tOUfBmxszV0hioKTouk+nc2AmeEWJ7zUZzM4t021v8w/Hvhx49I9ZtrDW2M5SV9L8ZPB0ye7MQSRQNWBq17kro=', -- password (runescaper)
    'da0b754c2a0a1d625bb6f62c70f2a0e7291ef4d953967ce015c55cfef0006c2e', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/runescaper.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    FALSE -- is admin
  ),
  (
    'noob111', -- username
    'pIHupAuCACVN9fQSfCrtaxM420ERW/V4b4ey4EeyvUNEDjS+D6Ikbe5qcFT7zlWAPZi53EtO1Vk0xqWXb7EHLcZPB0ye7MQSRQNWBq17kro=', -- password (noob111)
    '41f3d1b7fc22c018034362f118c9c48e1073f3b6f5edb558e26629203f7e38b5', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/noob.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    FALSE -- is admin
  ),
  (
    'expert', -- username
    '14/1YYWN0GOPGnSjtN/oAJhSw4BX46pfUSadEjo92hngJgExvfGV3lKy3neju9qoCJAqh50y3qkZzoMxf/szgMZPB0ye7MQSRQNWBq17kro=', -- password (expert)
    'cff654c226795b4a0eb642ab5f3e4caa0cd6be3259b5d9c88d544343db3fef45', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/expert.gif', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    FALSE -- is admin
  ),
  (
    'maurice', -- username
    'PPgGYg63KoqyYW85LMt3IY7o3Nfb4jQ/FPUR8SE9MabL+pUxFK4+gRFFeX0p20/DQv4Th1+Yo140d5mA3w7Jq8ZPB0ye7MQSRQNWBq17kro=', -- password (maurice)
    '6ad55e79808634fbd37a9809d13ed2a01a8eb4ff65449c0e3061bb795ea6f3e2', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/default_profile_pic.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    TRUE -- is admin
  ),
  (
    'joris', -- username
    'U7ow4/rBPyEZZNWW/hQQxAaToQKOe81Jm7Cd9pwBJNGgFRhhz3MijFDdj48snyq0HG1zMG8xrIXU9QdQpAb6qcZPB0ye7MQSRQNWBq17kro=', -- password (joris)
    '097cfbd9951a6574cf8151a4c02aec0c4aa5eceb1832f73b921a8e4949b704ed', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/default_profile_pic.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    TRUE -- is admin
  ),
  (
    'ilyas', -- username
    '7yHEsOKKlEQO55lngExG9auO6xtM728seX8EfAApHYu/fJyAPSZ9OFYVb2ocmI6Eg8wl79oN8i4r2RC2rC0WJMZPB0ye7MQSRQNWBq17kro=', -- password (ilyas)
    '3983974248ebcded3ee340ac2de855a037078ec9fcfb7ab00122d070271ff572', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/default_profile_pic.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    TRUE -- is admin
  ),
  (
    'melle', -- username
    'bbaqfRDGOJ25Q76FfC3WxTGY2p8UJk91K+qLRKu0pn0rkjFJ/PU+lbM9Y9Pv42vFoDQXn20RaOtg7VbaYRfaccZPB0ye7MQSRQNWBq17kro=', -- password (melle)
    'e9cc37a68417f9134cdbd755f3852425a6a5b6d9c5f7db7b3eab047d60513ecf', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/default_profile_pic.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    TRUE -- is admin
  ),
  (
    'daryl', -- username
    'iCoTqpY8bbkip64c+kwHTx10rtDzjd8RgjyQJkIhE0ULRRfKp3b1asYVROK+RRkjngqcLCtxkfW8wb5Bh2MejcZPB0ye7MQSRQNWBq17kro=', -- password (daryl)
    '42b4363d5cf2c94c499cc9eee6001a934b7a6d936e7c6f4790fa04f31e326d85', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/default_profile_pic.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    TRUE -- is admin
  ),
  (
    'johan', -- username
    'aGgYcgJsAtpoT8O6h7LGfJ/ahODYwrxKxqh8PdQnV/2N3Li7Lu2nHTEUUo4+esHZfTJ0waVXW8liaVUScWT7JMZPB0ye7MQSRQNWBq17kro=', -- password (johan)
    '44357731c71c489f180fb62634014969a6446112d36dcaf65662042243a1b1f8', -- password salt
    'restartcontactus@gmail.com', -- mail
    'images/default_profile_pic.png', -- profile picture
    'restartcontactus@gmail.com', -- payment mail
    NULL, -- inventory
    NULL, -- favorites
    NULL, -- order history
    now(), -- member since
    TRUE -- is admin
  );

INSERT INTO gamecategories (name, image, description, genre) VALUES
  (
    'Counter-Strike: Global Offensive', -- name
    'images/CSGO-Cover.jpg', -- url to image
    'Counter-Strike: Global Offensive (CS:GO) is a multiplayer first-person shooter video game developed by Hidden Path Entertainment and Valve Corporation. It is the fourth game in the Counter-Strike series and was released for Microsoft Windows, OS X, Xbox 360, and PlayStation 3 in August 2012, with the Linux version released in September 2014. The game pits two teams against each other: the Terrorists and the Counter-Terrorists. Both sides are tasked with eliminating the other while also completing separate objectives, the Terrorists, depending on the game mode, must either plant the bomb or defend the hostages, while the Counter-Terrorists must either prevent the bomb from being planted or rescue the hostages. There are six game modes, all of which have distinct characteristics specific to that mode.', -- description
    'FPS' -- genre
  ),
  (
    'Overwatch', -- name
    'images/Overwatch-Cover.jpg', -- url to image
    'Overwatch is a team-based multiplayer online first-person shooter video game developed and published by Blizzard Entertainment. It was released in May 2016 for Windows, PlayStation 4, and Xbox One. Overwatch assigns players into two teams of six, with each player selecting from a roster of over 20 characters, known in-game as "heroes", each with a unique style of play, whose roles are divided into four general categories: Offense, Defense, Tank, and Support. Players on a team work together to secure and defend control points on a map or escort a payload across the map in a limited amount of time. Players gain cosmetic rewards that do not affect gameplay, such as character skins and victory poses, as they play the game. The game was initially launched with casual play, with a competitive ranked mode, various ''arcade'' game modes, and a player-customizable server browser subsequently included following its release. Additionally, Blizzard has developed and added new characters, maps, and game modes post-release, while stating that all Overwatch updates will remain free, with the only additional cost to players being microtransactions to earn additional cosmetic rewards.', -- description
    'FPS' -- genre
  ),
  (
    'World of Warcraft', -- name
    'images/WoW-Cover.jpg', -- url to image
    'World of Warcraft (WoW) is a massively multiplayer online role-playing game (MMORPG) released in 2004 by Blizzard Entertainment. It is the fourth released game set in the Warcraft fantasy universe. World of Warcraft takes place within the Warcraft world of Azeroth, approximately four years after the events at the conclusion of Blizzard''s previous Warcraft release, Warcraft III: The Frozen Throne. Blizzard Entertainment announced World of Warcraft on September 2, 2001. The game was released on November 23, 2004, on the 10th anniversary of the Warcraft franchise.', -- description
    'MMORPG' -- genre
  ),
  (
    'League of Legends', -- name
    'images/LoL-Cover.jpg', -- url to image
    'League of Legends (abbreviated LoL) is a multiplayer online battle arena video game developed and published by Riot Games for Microsoft Windows and macOS. The game follows a freemium model and is supported by microtransactions, and was inspired by the Warcraft III: The Frozen Throne mod, Defense of the Ancients.', -- description
    'MOBA' -- genre
  ),
  (
    'Minecraft', -- name
    'images/Minecraft-Cover.jpg', -- url to image
    'Minecraft is a sandbox video game created and designed by Swedish game designer Markus "Notch" Persson, and later fully developed and published by Mojang. The creative and building aspects of Minecraft allow players to build with a variety of different cubes in a 3D procedurally generated world. Other activities in the game include exploration, resource gathering, crafting, and combat.', -- description
    'sandbox' -- genre
  ),
  (
    E'PLAYERUNKNOWN\'S BATTLEGROUNDS', -- name
    'images/PUBG.jpg', -- url to image
    'PlayerUnknown''s Battlegrounds (PUBG) is a multiplayer online battle royale game developed by PUBG Corporation, a subsidiary of Korean publisher Bluehole. The game is based on previous mods that were developed by Brendan "PlayerUnknown" Greene for other games using the 2000 film Battle Royale for inspiration, and expanded into a standalone game under Greene''s creative direction. In the game, up to one hundred players parachute onto an island and scavenge for weapons and equipment to kill others while avoiding getting killed themselves. The available safe area of the game''s map decreases in size over time, directing surviving players into tighter areas to force encounters. The last player or team standing wins the round.', -- description
    'FPS' -- genre
  ),
  (
    'Destiny 2', -- name
    'images/Destiny-2-Cover.jpg', -- url to image
    'Destiny 2 is an online-only multiplayer first-person shooter video game developed by Bungie and published by Activision. It was released for PlayStation 4 and Xbox One on September 6, 2017, followed by a Microsoft Windows version the following month. It is the sequel to 2014''s Destiny and its subsequent expansions. Set in a "mythic science fiction" world, the game features a multiplayer "shared-world" environment with elements of role-playing games. Players assume the role of a Guardian, protectors of Earth''s last safe city as they wield a power called Light to protect the Last City from different alien races. One of these races, the Cabal, led by the warlord Dominus Ghaul, infiltrate the Last City and strips all Guardians of their Light. The player sets out on a journey to regain their Light and find a way to defeat Ghaul and his Red Legion army and take back the Last City.', -- description
    'FPS' -- genre
  ),
  (
    'Hearthstone', -- name
    'images/Hearthstone-Cover.jpg', -- url to image
    'Hearthstone, originally known as Hearthstone: Heroes of Warcraft, is a free-to-play online collectible card video game developed and published by Blizzard Entertainment. Having been released worldwide on March 11, 2014, Hearthstone builds upon the already existing lore of the Warcraft series by using the same elements, characters, and relics. It was first released for Microsoft Windows and macOS, with support for iOS and Android devices being added later. The game features cross-platform play, allowing players on any device to compete with each other, restricted only by geographical region account limits.', -- description
    'other' -- genre
  ),
  (
    'Dota 2', -- name
    'images/Dota_2-Cover.png', -- url to image
    'Dota 2 is a free-to-play multiplayer online battle arena (MOBA) video game developed and published by Valve Corporation. The game is the stand-alone sequel to Defense of the Ancients (DotA), which was a community-created mod for Blizzard Entertainment''s Warcraft III: Reign of Chaos and its expansion pack, The Frozen Throne. Dota 2 is played in matches between two teams of five players, with each team occupying and defending their own separate base on the map. Each of the ten players independently controls a powerful character, known as a "hero", who all have unique abilities and differing styles of play. During a match, players collect experience points and items for their heroes in order to successfully battle the opposing team''s heroes, who are attempting to do the same to them. A team wins by being the first to destroy a large structure located in the opposing team''s base, called the "Ancient".', -- description
    'MOBA' -- genre
  ),
  (
    'Rocket League', -- name
    'images/Rocket_League-Cover.png', -- url to image
    'Rocket League is a vehicular soccer video game developed and published by Psyonix. The game was first released for Microsoft Windows and PlayStation 4 in July 2015, with ports for Xbox One, OS X, Linux, and Nintendo Switch later being released. In June 2016, 505 Games began distributing a physical retail version for PlayStation 4 and Xbox One, with Warner Bros. Interactive Entertainment taking over those duties by the end of 2017.', -- description
    'other' -- genre
  ),
  (
    'For Honor', -- name
    'images/For_Honor-Cover.png', -- url to image
    'For Honor is a hack and slash fighting game developed and published by Ubisoft for Microsoft Windows, PlayStation 4, and Xbox One. The game allows players to play the roles of historical forms of soldiers and warriors, including knights, samurai, and vikings within a medieval setting, controlled using a third-person perspective.', -- description
    'other' -- genre
  ),
  (
    'Call of Duty: WWII', -- name
    'images/CoD_WW2-Cover.png', -- url to image
    'Call of Duty: WWII is a first-person shooter video game developed by Sledgehammer Games and published by Activision. It is the fourteenth main installment in the Call of Duty series and was released worldwide on November 3, 2017 for Microsoft Windows, PlayStation 4 and Xbox One. It is the first title in the series to be set primarily during World War II since Call of Duty: World at War in 2008. The game is set in the European theatre, and is centered around a squad in the 1st Infantry Division, following their battles on the Western Front, and set mainly in the historical events of Operation Overlord: the multiplayer expands to different fronts not seen in the campaign.', -- description
    'FPS' -- genre
  ),
  (
    'Starcraft II', -- name
    'images/Starcraft_2-Cover.png', -- url to image
    'StarCraft II: Wings of Liberty is a military science fiction real-time strategy video game developed and published by Blizzard Entertainment. It was released worldwide in July 2010 for Microsoft Windows and Mac OS X. A sequel to the 1998 video game StarCraft and its expansion set Brood War, the game is split into three installments: the base game with the subtitle Wings of Liberty, an expansion pack Heart of the Swarm, and a stand-alone expansion pack Legacy of the Void.', -- description
    'other' -- genre
  ),
  (
    'RuneScape', -- name
    'images/RuneScape-Cover.png', -- url to image
    'RuneScape is a fantasy MMORPG developed and published by Jagex, released originally in January 2001. RuneScape can be used as a graphical browser game, implemented on the client-side in Java, and incorporates 3D rendering. Since the release of the NXT client in April 2016, a non-browser based version of the game that is written in C++ is also available. The game has had over 200 million accounts created and is recognised by the Guinness World Records as the world''s largest free MMORPG and the most-updated game.', -- description
    'MMORPG' -- genre
  ),
  (
    'Heroes of the Storm', -- name
    'images/Heroes_of_the_Storm-Cover.png', -- url to image
    'Heroes of the Storm (HotS) is a multiplayer online battle arena video game developed and published by Blizzard Entertainment for Microsoft Windows and macOS that was released on June 2, 2015. The game features heroes from Blizzard''s franchises including Warcraft, Diablo, StarCraft, The Lost Vikings, and Overwatch. The game uses both free-to-play and freemium models and is supported by micropayments, which can be used to purchase heroes, visual alterations for the heroes in the game, and mounts. Blizzard does not call the game a "multiplayer online battle arena" or an "action real-time strategy" because they feel it is something different with a broader playstyle: they refer to it as an online "hero brawler".', -- description
    'MOBA' -- genre
  ),
  (
    'Grand Theft Auto V', -- name
    'images/GTA5-Cover.png', -- url to image
    'Grand Theft Auto V is an action-adventure video game developed by Rockstar North and published by Rockstar Games. It was released in September 2013 for PlayStation 3 and Xbox 360, in November 2014 for PlayStation 4 and Xbox One, and in April 2015 for Microsoft Windows. It is the first main entry in the Grand Theft Auto series since 2008''s Grand Theft Auto IV. Set within the fictional state of San Andreas, based on Southern California, the single-player story follows three criminals and their efforts to commit heists while under pressure from a government agency. The open world design lets players freely roam San Andreas'' open countryside and the fictional city of Los Santos, based on Los Angeles.', -- description
    'sandbox' -- genre
  );

INSERT INTO gameaccounts (userid, gameid, visible, disabled, title, description, addedsince, canbuy, buyprice, cantrade, maillast, mailcurrent, passwordcurrent) VALUES
  (
    -- This gameaccount has been bought by admin and sold by default
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
    1.00, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    7, -- userid
    1, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Just my CSGO', -- title,
    'My CSGO account *cling*', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    100.00, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    4, -- userid
    1, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Better than Runescaper', -- title,
    'Runescaper...', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    27.00, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    5, -- userid
    1, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Better than Minecrafter', -- title,
    'Minecrafter...', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    32.00, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    6, -- userid
    1, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'CSGO: Better Worst Player', -- title,
    'This is the BEST Worst Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    1.00, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    7, -- userid
    2, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Pro Player', -- title,
    'This is a Pro Player account!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    70.00, -- buyprice
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
    6, -- userid
    2, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Overwatch Win Streak', -- title,
    'This account has a huge win streak!!!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    1.00, -- buyprice
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
    4, -- userid
    5, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Minecraft: Medium Player', -- title,
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
    4, -- userid
    5, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Hypixel MVP', -- title,
    'This is a Hypixel account with MVP rank!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    100.00, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    4, -- userid
    5, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Mineplex ULTRA', -- title,
    'This is a Mineplex account with ULTRA rank!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    25.00, -- buyprice
    FALSE, -- cantrade
    'lastmail@example.com', -- maillast
    'currentmail@example.com', -- mailcurrent
    'currentpassword' -- passwordcurrent
  ),
  (
    6, -- userid
    5, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'Minecraft: Worst Player', -- title,
    'I did my best!', -- description
    now(), -- addedsince
    TRUE, -- canbuy
    1.00, -- buyprice
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
    5, -- userid
    14, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'RS: Pro Player', -- title,
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
    5, -- userid
    14, -- gameid
    TRUE, -- visible
    FALSE, -- disabled
    'RS: Medium Player', -- title,
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

INSERT INTO orders (trackid, hasuser, userid, productid, price, couponcode, status, orderplaced) VALUES
  (
    '54947df8-0e9e-4471-a2f9-9af509fb5889', -- trackid
    TRUE, -- hasuser
    1, -- userid
    1, -- productid
    105.00, -- price
    '', -- couponcode
    0, -- status
    now() -- orderplaced
  );

INSERT INTO reviews (userreceiverid, usersenderid, title, description, rating) VALUES
  (
    '1', -- userreceiverid
    '2', -- usersenderid
    'Great', -- Title
    'nice.', -- desription
    '5' --  rating
  ),
  (
    '1', -- userreceiverid
    '7', -- usersenderid
    'Fast and really reliable!', -- Title
    'thank you!', -- desription
    '4' --  rating
  ),
  (
    '1', -- userreceiverid
    '4', -- usersenderid
    'it was okay', -- Title
    'Got my account in a few minutes.', -- desription
    '3' --  rating
  ),
  (
    '1', -- userreceiverid
    '5', -- usersenderid
    'very fast!', -- Title
    'Good service and a great seller!', -- desription
    '5' --  rating
  ),
  (
    '1', -- userreceiverid
    '6', -- usersenderid
    'very fast!', -- Title
    'Good service and a great seller!', -- desription
    '5' --  rating
  ),
  (
    '1', -- userreceiverid
    '3', -- usersenderid
    'fixed my problem', -- Title
    'Had a problem with the account. The seller helped me out and everything was fixed afterwards :D', -- desription
    '3' --  rating
  ),
  (
    '1', -- userreceiverid
    '8', -- usersenderid
    'satisfying', -- Title
    'Thank you very much for the excellent service', -- desription
    '5' --  rating
  ),
  (
    '1', -- userreceiverid
    '8', -- usersenderid
    'Item as described.', -- Title
    'ty', -- desription
    '3' --  rating
  ),
  (
    '1', -- userreceiverid
    '9', -- usersenderid
    'great!!!', -- Title
    'Fast like always, thanks Restart :) 5 * * * * *', -- desription
    '5' --  rating
  )
  ,
  (
    '1', -- userreceiverid
    '10', -- usersenderid
    'dziękuję', -- Title
    'Wszystko dziala pozytywanie jak najbardziej Polecam 100% Dobra cena za produkt', -- desription
    '4' --  rating
  ),
  (
    '1', -- userreceiverid
    '11', -- usersenderid
    'Thanks!', -- Title
    'Nice and quick!', -- desription
    '5' --  rating
  ),
  (
    '1', -- userreceiverid
    '12', -- usersenderid
    'Grazie', -- Title
    'Ho avuto un piccolo problema con la account, ma Restart ha risolto e tutto funziona correttamente', -- desription
    '5' --  rating
  ),
  (
    '1', -- userreceiverid
    '13', -- usersenderid
    'Beast!', -- Title
    'Noice. Like a bawse', -- desription
    '5' --  rating
  ),
  (
    '2', -- userreceiverid
    '3', -- usersenderid
    'perfect', -- Title
    'Flawless ty', -- desription
    '4' --  rating
  ),
  (
    '2', -- userreceiverid
    '6', -- usersenderid
    'very nice', -- Title
    'The account details arrived immediatly. Wasnt my first purchase and definitely wont be the last one.', -- desription
    '5' --  rating
  ),
  (
    '2', -- userreceiverid
    '8', -- usersenderid
    'ok', -- Title
    'ok.', -- desription
    '3' --  rating
  ),
  (
    '2', -- userreceiverid
    '10', -- usersenderid
    'good', -- Title
    'thank you very much', -- desription
    '3' --  rating
  );

--- !Downs