// Element tanımları
const responseElement = document.getElementById('apiResponse');
const fetchHelloButton = document.getElementById('fetchHello');
const resetButton = document.getElementById('resetResponse');
const faviconLink = document.querySelector('link[rel="icon"]');
const themeToggle = document.getElementById('themeToggle');

// Reset butonunun durumunu güncelle
function toggleResetButton() {
  resetButton.disabled = !responseElement.textContent.trim();
}

// Tema ikonunu güncelle
function updateFaviconBasedOnTheme(theme) {
  faviconLink.href = theme === 'dark'
    ? "/favicons/favicon-dark.ico"
    : "/favicons/favicon-light.ico";
}

function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  document.documentElement.classList.toggle('dark-theme', theme === 'dark');
  updateFaviconBasedOnTheme(theme);
  localStorage.setItem('theme', theme);
}

themeToggle.addEventListener('change', () => {
  const theme = themeToggle.checked ? 'dark' : 'light';
  applyTheme(theme);
});

async function fetchHelloMessage() {
  responseElement.textContent = "Loading...";
  responseElement.className = "alert alert-info d-block";
  toggleResetButton();

  try {
    const response = await fetch('/api/v1/hello');
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    const data = await response.json();
    responseElement.textContent = data.message;
    responseElement.className = "alert alert-success d-block";
  } catch (error) {
    responseElement.textContent = `Error: ${error.message}`;
    responseElement.className = "alert alert-danger d-block";
  } finally {
    toggleResetButton();
  }
}

function resetResponse() {
  responseElement.textContent = "";
  responseElement.className = "alert d-none";
  toggleResetButton();
}

fetchHelloButton.addEventListener('click', fetchHelloMessage);
resetButton.addEventListener('click', resetResponse);

const savedTheme = localStorage.getItem('theme') || 'light';
themeToggle.checked = savedTheme === 'dark';
applyTheme(savedTheme);

toggleResetButton();
