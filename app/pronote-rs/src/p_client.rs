use crate::Client;
use crate::Error;
use crate::communication::Communication;
use crate::data::*;

#[derive(uniffi::Object)]
pub struct PronoteClient {
    c: tokio::sync::Mutex<crate::Client>,
}

#[uniffi::export(async_runtime = "tokio")]
impl PronoteClient {
    #[uniffi::constructor]
    pub async fn login(url: String, username: String, password: String) -> Result<Self, Error> {
        Ok(Self {
            c: tokio::sync::Mutex::new(Client::password_login(&url, &username, &password).await?),
        })
    }

    #[uniffi::constructor]
    pub async fn token_login(
        url: String,
        username: String,
        token: String,
        uuid: String,
        nav_identifier: String,
    ) -> Result<Self, Error> {
        Ok(Self {
            c: tokio::sync::Mutex::new(
                Client::token_login(&url, &username, &token, &uuid, &nav_identifier).await?,
            ),
        })
    }

    #[uniffi::constructor]
    pub async fn qr_login(qr_code: String, pin: String, uuid: String) -> Result<Self, Error> {
        Ok(Self {
            c: tokio::sync::Mutex::new(Client::qr_login(&qr_code, &pin, &uuid).await?),
        })
    }

    #[uniffi::constructor]
    pub async fn mock() -> Result<Self, Error> {
        let parameters = Client::mock_params().await?;
        let user_parameters = Client::mock_user_params().await?;

        Ok(Self {
            c: tokio::sync::Mutex::new(Client {
                username: String::new(),
                password: String::new(),
                communication: Communication::mock(),
                parameters,
                user_parameters: Some(user_parameters),
                nav_identifier: String::new(),
                next_token: String::new(),
                login_mode: LoginMode::Password,
                uuid: String::new(),
            }),
        })
    }

    pub async fn params(&self) -> Params {
        self.c.lock().await.parameters.clone()
    }

    pub async fn user_params(&self) -> Option<UserParams> {
        self.c.lock().await.user_parameters.clone()
    }

    pub async fn next_token(&self) -> String {
        self.c.lock().await.next_token.clone()
    }

    pub async fn nav_identifier(&self) -> String {
        self.c.lock().await.nav_identifier.clone()
    }

    pub async fn uuid(&self) -> String {
        self.c.lock().await.uuid.clone()
    }

    pub async fn get_week(&self, date: String) -> u8 {
        self.c.lock().await.get_week(&date)
    }

    pub async fn homework(&self, first_week: u8, last_week: u8) -> Result<Vec<Homework>, Error> {
        self.c.lock().await.homework(first_week, last_week).await
    }

    pub async fn timetable(&self, week: u8) -> Result<Vec<Vec<Lesson>>, Error> {
        self.c.lock().await.timetable(week).await
    }

    pub async fn home(&self, date: String) -> Result<Home, Error> {
        self.c.lock().await.home(&date).await
    }

    pub async fn grades(&self, period: Period) -> Result<Grades, Error> {
        self.c.lock().await.grades(&period).await
    }

    pub async fn documents(&self, first_week: u8, last_week: u8) -> Result<Resources, Error> {
        self.c.lock().await.documents(first_week, last_week).await
    }

    pub async fn agenda(&self) -> Result<Agenda, Error> {
        let mut guard = self.c.lock().await;
        let result = guard.agenda().await;

        result
    }

    pub async fn news(&self) -> Result<News, Error> {
        self.c.lock().await.news().await
    }

    pub async fn discussions(&self) -> Result<Discussions, Error> {
        self.c.lock().await.discussions().await
    }

    pub async fn messages(&self, possessions: Vec<Possession>) -> Result<Messages, Error> {
        self.c.lock().await.messages(&possessions).await
    }

    pub async fn news_item_content(&self, news_item_id: String) -> Result<Vec<Question>, Error> {
        self.c.lock().await.news_item_content(&news_item_id).await
    }

    pub async fn set_done(&self, homework_id: String, done: bool) -> Result<(), Error> {
        self.c.lock().await.set_done(&homework_id, done).await
    }

    pub async fn staff(&self) -> Result<Vec<StaffTeacher>, Error> {
        self.c.lock().await.staff().await
    }

    pub async fn geolocation(&self, lat: f64, long: f64) -> Result<Vec<School>, Error> {
        Client::geolocation(lat, long).await
    }

    pub async fn mock_home(&self, _date: String) -> Result<Home, Error> {
        Client::mock_home(&Client::mock_params().await?).await
    }

    pub async fn mock_homework(
        &self,
        _first_week: u8,
        _last_week: u8,
    ) -> Result<Vec<Homework>, Error> {
        Client::mock_homework().await
    }

    pub async fn mock_timetable(&self, _week: u8) -> Result<Vec<Vec<Lesson>>, Error> {
        Client::mock_timetable(&Client::mock_params().await?).await
    }

    pub async fn mock_grades(&self, period: Period) -> Result<Grades, Error> {
        Client::mock_grades(&period).await
    }

    pub async fn mock_documents(
        &self,
        _first_week: u8,
        _last_week: u8,
    ) -> Result<Resources, Error> {
        Client::mock_documents().await
    }

    pub async fn mock_agenda(&self) -> Result<Agenda, Error> {
        Client::mock_agenda().await
    }

    pub async fn mock_news(&self) -> Result<News, Error> {
        Client::mock_news().await
    }

    pub async fn mock_discussions(&self) -> Result<Discussions, Error> {
        Client::mock_discussions().await
    }

    pub async fn mock_messages(&self, _possessions: Vec<Possession>) -> Result<Messages, Error> {
        Client::mock_messages().await
    }

    pub async fn mock_news_item_content(
        &self,
        _news_item_id: String,
    ) -> Result<Vec<Question>, Error> {
        Client::mock_news_item_content().await
    }

    pub async fn mock_set_done(&self, _homework_id: String, _done: bool) -> Result<(), Error> {
        // do nothing
        Ok(())
    }

    pub async fn mock_staff(&self) -> Result<Vec<StaffTeacher>, Error> {
        Client::mock_staff().await
    }

    pub async fn mock_geolocation(&self) -> Result<Vec<School>, Error> {
        Client::mock_geolocation().await
    }
}
