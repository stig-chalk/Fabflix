USE `moviedb`;
DROP procedure IF EXISTS `add_movie`;

DELIMITER $$
USE `moviedb`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `add_movie`(
    in movie_title varchar(100),
    in movie_year int(11),
    in movie_director varchar(100),
    in star_name varchar(10),
    in genre_name varchar(32)
)
BEGIN

	set @max_movie_id = (select concat('tt',lpad(convert(substring(max(id),3),  unsigned) + 1, 7, 0)) as num from moviedb.movies);
	INSERT INTO moviedb.movies VALUES (@max_movie_id, movie_title, movie_year, movie_director);
    set @movie_id = @max_movie_id;
	
	set @max_star_id = (select concat('nm',lpad(convert(substring(max(id),3),  unsigned) + 1, 7, 0)) as num from moviedb.stars);
	set @star_id = (select concat('nm',lpad(convert(substring(id,3),  unsigned) , 7, 0)) as num from moviedb.stars where name = star_name limit 1);
	
	IF @star_id is null then
	begin
		INSERT INTO moviedb.stars VALUES (@max_star_id,star_name, null);
        set @star_id = @max_star_id;
	end;
	end if;
	INSERT INTO moviedb.stars_in_movies VALUES (@star_id,@max_movie_id);
    
    
	set @max_genre_id = (select max(id)+1 from moviedb.genres);
	set @genre_id = (select id from moviedb.genres where name = genre_name limit 1);
    
    if @genre_id is null then
    begin
		INSERT INTO moviedb.genres VALUES (@max_genre_id, genre_name);
		set @genre_id = @max_genre_id;
    end;
    end if;
    INSERT INTO moviedb.genres_in_movies VALUES (@genre_id,@max_movie_id);

END$$

DELIMITER ;

