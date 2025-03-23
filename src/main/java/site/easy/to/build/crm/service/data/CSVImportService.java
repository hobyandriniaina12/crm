package site.easy.to.build.crm.service.data;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.EmailTemplateRepository;
import site.easy.to.build.crm.repository.FileRepository;
import site.easy.to.build.crm.repository.GoogleDriveFileRepository;
import site.easy.to.build.crm.repository.LeadActionRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.repository.OAuthUserRepository;
import site.easy.to.build.crm.repository.RoleRepository;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.repository.UserProfileRepository;
import site.easy.to.build.crm.repository.UserRepository;
import site.easy.to.build.crm.repository.settings.ContractEmailSettingsRepository;
import site.easy.to.build.crm.repository.settings.LeadEmailSettingsRepository;
import site.easy.to.build.crm.repository.settings.TicketEmailSettingsRepository;
import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.EmailTemplate;
import site.easy.to.build.crm.entity.File;
import site.easy.to.build.crm.entity.GoogleDriveFile;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.LeadAction;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.Role;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.UserProfile;
import site.easy.to.build.crm.entity.settings.ContractEmailSettings;
import site.easy.to.build.crm.entity.settings.LeadEmailSettings;
import site.easy.to.build.crm.entity.settings.TicketEmailSettings;
import site.easy.to.build.crm.repository.ContractRepository;
import site.easy.to.build.crm.repository.CustomerLoginInfoRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class CSVImportService {

    private final Map<String, Object> repositories = new HashMap<>();
    private final Map<String, Class<?>> entityClasses = new HashMap<>();

    public CSVImportService(
            CustomerRepository customerRepository,
            CustomerLoginInfoRepository customerLoginInfoRepository,
            UserRepository userRepository,
            OAuthUserRepository oauthUser,
            UserProfileRepository userProfile,
            RoleRepository role,
            EmailTemplateRepository emailTemplate,
            LeadRepository lead,
            TicketRepository ticket,
            ContractRepository contract,
            ContractEmailSettingsRepository contractEmailSettings,
            LeadActionRepository leadAction,
            LeadEmailSettingsRepository leadEmailSettings,
            TicketEmailSettingsRepository ticketEmailSettings,
            FileRepository file,
            GoogleDriveFileRepository googleDriveFile
    ) {
        // Initialisation des repositories
        repositories.put("customer", customerRepository);
        repositories.put("customer_login_info", customerLoginInfoRepository);
        repositories.put("users", userRepository);
        repositories.put("oauth_users", oauthUser);
        repositories.put("user_profile", userProfile);
        repositories.put("roles", role);
        repositories.put("email_template", emailTemplate);
        repositories.put("trigger_lead", lead);
        repositories.put("trigger_ticket", ticket);
        repositories.put("trigger_contract", contract);
        repositories.put("contract_settings", contractEmailSettings);
        repositories.put("lead_action", leadAction);
        repositories.put("lead_settings", leadEmailSettings);
        repositories.put("ticket_settings", ticketEmailSettings);
        repositories.put("file", file);
        repositories.put("google_drive_file", googleDriveFile);

        // Initialisation des entités associées
        entityClasses.put("customer", Customer.class);
        entityClasses.put("customer_login_info", CustomerLoginInfo.class);
        entityClasses.put("users", User.class);
        entityClasses.put("oauth_users", OAuthUser.class);
        entityClasses.put("user_profiles", UserProfile.class);
        entityClasses.put("roles", Role.class);
        entityClasses.put("email_template", EmailTemplate.class);
        entityClasses.put("trigger_lead", Lead.class);
        entityClasses.put("trigger_ticket", Ticket.class);
        entityClasses.put("trigger_contract", Contract.class);
        entityClasses.put("contract_settings", ContractEmailSettings.class);
        entityClasses.put("lead_action", LeadAction.class);
        entityClasses.put("lead_settings", LeadEmailSettings.class);
        entityClasses.put("ticket_settings", TicketEmailSettings.class);
        entityClasses.put("file", File.class);
        entityClasses.put("google_drive_file", GoogleDriveFile.class);

        // Vérification de l'initialisation des repositories
        repositories.forEach((key, value) -> 
            System.out.println("Table: " + key + " -> Repository: " + value.getClass().getSimpleName()));
    }

    public void importCSV(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                String tableName = record.get("table").toLowerCase(); // Nom de la table dans le CSV

                if (repositories.containsKey(tableName) && entityClasses.containsKey(tableName)) {
                    Object entity = mapRecordToEntity(record, entityClasses.get(tableName));
                    Object repository = repositories.get(tableName);

                    System.out.println("Importation pour la table: " + tableName);
                    saveEntity(entity, repository);
                } else {
                    System.err.println("Aucun repository trouvé pour la table: " + tableName);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'importation CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveEntity(Object entity, Object repository) throws Exception {
        if (repository == null) {
            throw new IllegalArgumentException("Repository est null pour l'entité: " + entity.getClass().getSimpleName());
        }
        Method saveMethod = repository.getClass().getMethod("save", Object.class);
        saveMethod.invoke(repository, entity);
    }

    private Object mapRecordToEntity(CSVRecord record, Class<?> entityClass) throws Exception {
        Object entity = entityClass.getDeclaredConstructor().newInstance();

        for (Field field : entityClass.getDeclaredFields()) {
            String fieldName = field.getName();
            if (record.isMapped(fieldName)) {
                field.setAccessible(true);
                field.set(entity, convertValue(field, record.get(fieldName)));
            }
        }
        return entity;
    }

    private Object convertValue(Field field, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        Class<?> type = field.getType();
        try {
            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(value);
            } else if (type == long.class || type == Long.class) {
                return Long.parseLong(value);
            } else if (type == double.class || type == Double.class) {
                return Double.parseDouble(value);
            } else if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(value);
            } else {
                return value;
            }
        } catch (Exception e) {
            System.err.println("Erreur de conversion pour le champ: " + field.getName() + " avec la valeur: " + value);
            return null;
        }
    }
}