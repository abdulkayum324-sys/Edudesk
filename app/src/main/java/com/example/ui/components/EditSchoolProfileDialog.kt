package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolProfile
import com.example.ui.theme.NavyPrimary

@Composable
fun EditSchoolProfileDialog(
    currentProfile: SchoolProfile,
    onDismiss: () -> Unit,
    onConfirm: (SchoolProfile) -> Unit
) {
    var schoolName by remember { mutableStateOf(currentProfile.schoolName) }
    var schoolMotto by remember { mutableStateOf(currentProfile.schoolMotto) }
    var campusAddress by remember { mutableStateOf(currentProfile.campusAddress) }
    var contactPhone by remember { mutableStateOf(currentProfile.contactPhone) }
    var contactEmail by remember { mutableStateOf(currentProfile.contactEmail) }
    var academicSession by remember { mutableStateOf(currentProfile.academicSession) }
    var principalName by remember { mutableStateOf(currentProfile.principalName) }
    var currencySymbol by remember { mutableStateOf(currentProfile.currencySymbol) }
    var affiliationCode by remember { mutableStateOf(currentProfile.affiliationCode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = NavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Edit School & Institution Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Update institution branding, billing name, and contact details shown across reports and invoices.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("School Name *") },
                    placeholder = { Text("e.g. Oakridge International Academy") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("school_name_input")
                )

                OutlinedTextField(
                    value = schoolMotto,
                    onValueChange = { schoolMotto = it },
                    label = { Text("School Tagline / Motto") },
                    placeholder = { Text("Excellence in Academic Leadership") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = campusAddress,
                    onValueChange = { campusAddress = it },
                    label = { Text("Campus Address *") },
                    placeholder = { Text("Main Academic Campus, Kathmandu, Nepal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = academicSession,
                        onValueChange = { academicSession = it },
                        label = { Text("Academic Session") },
                        placeholder = { Text("Session 2025-2026") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = { currencySymbol = it },
                        label = { Text("Currency Symbol") },
                        placeholder = { Text("Rs. or रू") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = principalName,
                        onValueChange = { principalName = it },
                        label = { Text("Principal / Headmaster") },
                        placeholder = { Text("Dr. Arthur Pendelton") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = affiliationCode,
                        onValueChange = { affiliationCode = it },
                        label = { Text("Affiliation / Reg #") },
                        placeholder = { Text("NEB-REG-48201") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (schoolName.isNotBlank()) {
                        val updated = currentProfile.copy(
                            schoolName = schoolName.trim(),
                            schoolMotto = schoolMotto.trim(),
                            campusAddress = campusAddress.trim(),
                            contactPhone = contactPhone.trim(),
                            contactEmail = contactEmail.trim(),
                            academicSession = academicSession.trim(),
                            principalName = principalName.trim(),
                            currencySymbol = currencySymbol.trim(),
                            affiliationCode = affiliationCode.trim()
                        )
                        onConfirm(updated)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("save_school_profile_btn")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
