--liquibase formatted sql

--changeset alromos:1
CREATE TABLE Alromos.TestNumber
(
    TestNumberID INT NOT NULL,
    Description NVARCHAR(10) NOT NULL,
    CONSTRAINT PK_TestNumber_TestNumberID PRIMARY KEY (TestNumberID)
);
