CREATE TABLE IF NOT EXISTS t_Airbnb_data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(512) DEFAULT "",
    rating_average VARCHAR(32) DEFAULT "",
    price VARCHAR(32) DEFAULT "",
    tax_price VARCHAR(32) DEFAULT "",
    checkin VARCHAR(32) DEFAULT "",
    checkout VARCHAR(32) DEFAULT "",
    guest_num VARCHAR(32) DEFAULT ""
);
