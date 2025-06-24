const htmlInput = document.getElementById('html-input');
const jsonInput = document.getElementById('json-input');
const editSection = document.getElementById('edit-section');
const columnsInput = document.getElementById('columns-input');
const previewArea = document.getElementById('preview-area').attachShadow({ mode: 'open' });

let lastValidPreview = ''; // Store the last valid preview HTML

/**
 * Updates the preview area based on the HTML and JSON input.
 */
function updatePreview() {
    const style = document.createElement('style');
    style.textContent = `
        :host {
            all: initial; /* Reset all inherited styles */
            display: block; /* Ensure proper layout */
        }
    `;
    previewArea.appendChild(style);

    const html = htmlInput.value.trim();
    const jsonText = jsonInput.value.trim();
    let parsedData;

    // Basic validation: check if HTML is non-empty
    if (!html) {
        editSection.style.border = '2px solid red';
        previewArea.innerHTML = lastValidPreview || 'No valid preview available';
        return;
    }

    // Validate JSON
    try {
        parsedData = JSON.parse(jsonText || '{}');
    } catch (e) {
        editSection.style.border = '2px solid red';
        previewArea.innerHTML = lastValidPreview || 'No valid preview available';
        return;
    }

    let previewHtml = '';
    let isValid = true;

    if (Array.isArray(parsedData)) {
        // Handle array of objects
        const dataArray = parsedData;
        const columns = Number(columnsInput.value) || 1; // Use input value, default to 1

        // Validate each array element
        for (const data of dataArray) {
            if (!data || typeof data !== 'object') {
                isValid = false;
                break;
            }
        }

        if (!isValid || dataArray.length === 0) {
            editSection.style.border = '2px solid red';
            previewArea.innerHTML = lastValidPreview || 'No valid preview available';
            return;
        }

        // Generate HTML for each array element
        const renderedItems = dataArray.map(data => {
            let itemHtml = html;
            for (const key in data) {
                const placeholder = '{' + key + '}';
                const value = data[key];
                itemHtml = itemHtml.replace(new RegExp(placeholder, 'g'), value);
            }
            return `<div style="display: inline-block; vertical-align: top;">${itemHtml}</div>`;
        });

        // Wrap in a grid container
        previewHtml = `
            <div style="display: grid; grid-template-columns: repeat(${columns}, auto); gap: 0px; justify-content: start;">
                ${renderedItems.join('')}
            </div>
        `;
    } else {
        // Handle single object
        const data = parsedData;
        if (!data || typeof data !== 'object') {
            editSection.style.border = '2px solid red';
            previewArea.innerHTML = lastValidPreview || 'No valid preview available';
            return;
        }

        previewHtml = html;
        for (const key in data) {
            const placeholder = '{' + key + '}';
            const value = data[key];
            previewHtml = previewHtml.replace(new RegExp(placeholder, 'g'), value);
        }
    }

    // Update last valid state and preview
    lastValidPreview = previewHtml;
    previewArea.innerHTML = previewHtml;
    editSection.style.border = '1px solid #ccc'; // Reset to default border
}

/**
 * Takes the HTML content from the preview area and renders it as a PNG image.
 * This function uses the html2canvas library.
 * It handles external images by fetching them through a CORS proxy and embedding them as Data URLs.
 */
async function cookImage() {
    const cookButton = document.getElementById('cook-button');
    if (!cookButton) {
        console.error('"Cook" button not found!');
        return;
    }
    const originalButtonText = cookButton.textContent;
    cookButton.textContent = 'Cooking...';
    cookButton.disabled = true;

    const previewArea = document.getElementById('preview-area');
    // We create a clone of the node to avoid modifying the live preview while processing images.
    const clone = previewArea.cloneNode(true);

    // The clone must be in the DOM to be rendered, but we can make it invisible.
    clone.style.position = 'absolute';
    clone.style.left = '-9999px';
    clone.style.top = '0px';
    clone.style.width = `${previewArea.scrollWidth}px`; // Ensure it has a defined width
    document.body.appendChild(clone);


    // Helper function to fetch an image via a CORS proxy and convert it to a data URL.
    const convertImgToDataURL = async (imgElement) => {
        // This function embeds the image data directly into the HTML, which allows
        // html2canvas to render images from other domains.
        // NOTE: Public proxies are for development/demonstration only and are not reliable for production use.
        const proxyUrl = 'https://api.allorigins.win/raw?url=';
        const imageUrl = imgElement.src;

        try {
            // Fetch the image through the proxy
            const response = await fetch(proxyUrl + encodeURIComponent(imageUrl));
            if (!response.ok) throw new Error('Failed to fetch image through proxy');
            const blob = await response.blob();

            // Convert the image blob to a base64 Data URL
            return new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onloadend = () => {
                    imgElement.src = reader.result; // Replace the original src with the data URL
                    resolve();
                };
                reader.onerror = reject;
                reader.readAsDataURL(blob);
            });
        } catch (error) {
            console.error(`Could not process image: ${imageUrl}`, error);
            // If an image fails, replace it with a placeholder.
            imgElement.src = `https://placehold.co/200x200/ff0000/ffffff?text=Image+Load+Failed`;
            // Resolve the promise anyway so that one failed image doesn't stop the whole process.
            return Promise.resolve();
        }
    };


    const images = Array.from(clone.querySelectorAll('img'));
    const imagePromises = images.map(img => convertImgToDataURL(img));

    try {
        // Wait for all images to be fetched and replaced with data URLs
        await Promise.all(imagePromises);

        // Now that images are embedded, generate the canvas from our invisible clone.
        const canvas = await html2canvas(clone, {
            useCORS: true, // Still useful for other cross-origin content like web fonts
            allowTaint: true, // Allows cross-origin images to taint the canvas
            logging: false, // Set to true for debugging
            scale: 2, // Render at a higher resolution for better quality
        });

        // Create a temporary link to trigger the download of the canvas image.
        const link = document.createElement('a');
        link.download = 'cooked-image.png';
        link.href = canvas.toDataURL('image/png');
        link.click();

    } catch (error) {
        console.error('Error during the image cooking process:', error);
        alert('Failed to generate the image. See the browser console for more details.');
    } finally {
        // Clean up: remove the cloned element and restore the button's state.
        document.body.removeChild(clone);
        cookButton.textContent = originalButtonText;
        cookButton.disabled = false;
    }
}


function loadTemplateIntoEditor(button) {
    // Get the data attributes from the button
    var name = button.getAttribute('data-card-name');
    var cardStructure = button.getAttribute('data-card-structure');
    var cardValue = button.getAttribute('data-card-value');

    // Populate the textareas
    document.getElementById('name-input').value = name || '';
    document.getElementById('html-input').value = cardStructure || '';
    document.getElementById('json-input').value = cardValue || '';

    updatePreview(); // Update preview with loaded template
}

function deleteTemplate(button) {
    // Get the template ID from the data-card-id attribute
    var templateId = button.getAttribute('data-card-id');

    // Make an AJAX DELETE request to the server
    fetch('/card-templates/' + encodeURIComponent(templateId), {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to delete template');
            }
            // Remove the parent <li> element from the DOM
            var listItem = button.closest('li');
            if (listItem) {
                listItem.remove();
            }
        })
        .catch(error => {
            console.error('Error deleting template:', error);
            alert('Failed to delete template');
        });
}

function updateTemplate() {
    // Collect form data from input fields
    var formData = {
        name: document.getElementById('name-input').value,
        cardStructure: document.getElementById('html-input').value,
        cardValue: document.getElementById('json-input').value
    };

    // Make an AJAX POST request to the server
    fetch('/card-templates/', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to save template');
            }
            // Fetch the updated templates list
            return fetch('/card-templates/cards');
        })
        .then(response => response.text())
        .then(html => {
            // Update the templates list in the DOM
            var templatesList = document.getElementById('templates-list');
            if (templatesList) {
                templatesList.innerHTML = html;
            }
        })
        .catch(error => {
            console.error('Error saving template:', error);
            alert('Failed to save template');
        });
}

document.addEventListener('DOMContentLoaded', function() {

    htmlInput.addEventListener('input', updatePreview);
    jsonInput.addEventListener('input', updatePreview);
    columnsInput.addEventListener('input', updatePreview);
    updatePreview(); // Initial call to render preview with default values, if any
});
