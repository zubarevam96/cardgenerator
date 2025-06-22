const htmlInput = document.getElementById('html-input');
const jsonInput = document.getElementById('json-input');
const previewArea = document.getElementById('preview-area');
const editSection = document.getElementById('edit-section');
let lastValidPreview = ''; // Store the last valid preview HTML

function updatePreview() {
    const html = htmlInput.value.trim();
    const jsonText = jsonInput.value.trim();
    let data;

    // Basic validation: check if HTML is non-empty
    if (!html) {
        editSection.style.border = '2px solid red';
        previewArea.innerHTML = lastValidPreview || 'No valid preview available';
        return;
    }

    // Validate JSON
    try {
        data = JSON.parse(jsonText || '{}');
    } catch (e) {
        editSection.style.border = '2px solid red';
        previewArea.innerHTML = lastValidPreview || 'No valid preview available';
        return;
    }

    // If both inputs are valid, update preview and reset border
    let previewHtml = html;
    for (const key in data) {
        const placeholder = '{' + key + '}';
        const value = data[key];
        previewHtml = previewHtml.replace(new RegExp(placeholder, 'g'), value);
    }

    // Update last valid state and preview
    lastValidPreview = previewHtml;
    previewArea.innerHTML = previewHtml;
    editSection.style.border = '1px solid #ccc'; // Reset to default border
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
    updatePreview(); // Initial call to render preview with default values, if any
});