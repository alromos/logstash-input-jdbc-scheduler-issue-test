--liquibase formatted sql

--changeset alromos:2
DECLARE @pipeline_id INT = 0
WHILE @pipeline_id < 60
BEGIN
  INSERT INTO Alromos.TestNumber
    (TestNumberID, Description)
    VALUES
    (
      @pipeline_id,
      CONVERT(NVARCHAR(10), @pipeline_id)
    )
   SET @pipeline_id = @pipeline_id + 1
END;
