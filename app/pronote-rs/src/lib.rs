uniffi::setup_scaffolding!();

mod client;
mod communication;
mod data;
mod encryption;
mod helpers;

mod p_client;

pub use client::Client;
pub use data::*;

#[derive(Debug, uniffi::Error)]
pub enum Error {
    InvalidCredentials,
    InvalidStartPage,
    Expired,
    UnsupportedClient,
    UnauthorizedTab(u64),
    InvalidAction,
    ConnectionFailed,
    Pronote { code: i64, details: String },
    InvalidResponse,
}

impl std::fmt::Display for Error {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::InvalidCredentials => write!(f, "Invalid credentials"),
            Self::InvalidStartPage => write!(f, "Invalid start page"),
            Self::Expired => write!(f, "Session expired"),
            Self::UnsupportedClient => write!(f, "Unsupported client"),
            Self::UnauthorizedTab(tab) => write!(f, "Unauthorized tab: {tab}"),
            Self::InvalidAction => write!(f, "Invalid action"),
            Self::ConnectionFailed => write!(f, "Connection failed"),
            Self::Pronote { code, details } => {
                write!(f, "Pronote error {code}: {details}")
            }
            Self::InvalidResponse => write!(f, "Invalid response"),
        }
    }
}

