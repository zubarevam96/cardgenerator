function loadTemplateIntoEditor(button) {
    // Get the data attributes from the button
    var name = button.getAttribute('data-card-name');
    var cardStructure = button.getAttribute('data-card-structure');
    var cardValue = button.getAttribute('data-card-value');

    // Populate the textareas
    document.getElementById('name-input').value = name || '';
    document.getElementById('html-input').value = cardStructure || '';
    document.getElementById('json-input').value = cardValue || '';
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